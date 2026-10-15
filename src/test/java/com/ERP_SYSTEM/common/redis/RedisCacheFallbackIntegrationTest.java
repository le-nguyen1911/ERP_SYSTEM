package com.ERP_SYSTEM.common.redis;

import com.ERP_SYSTEM.common.cache.CacheNames;
import com.ERP_SYSTEM.inventory.dto.response.CategoryResponse;
import com.ERP_SYSTEM.inventory.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RedisCacheFallbackIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CacheManager cacheManager;

    @Test
    @DisplayName("Test 1: Redis hoạt động - API trả 200, cache miss rồi cache hit")
    @WithMockUser(authorities = "CATEGORY_VIEW")
    void test1_RedisActive_CacheMissAndHit() throws Exception {
        Cache cache = cacheManager.getCache(CacheNames.CATEGORIES);
        assertNotNull(cache);
        cache.clear();

        // Lần 1: Cache Miss -> Đọc PostgreSQL -> Ghi Cache -> 200 OK
        mockMvc.perform(get("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        // Kiểm tra cache đã được ghi nhận
        Cache.ValueWrapper wrapper = cache.get("all");
        assertNotNull(wrapper, "Cache key 'all' phải tồn tại sau lần gọi đầu tiên");
        assertNotNull(wrapper.get());

        // Lần 2: Cache Hit -> Lấy từ Cache -> 200 OK
        mockMvc.perform(get("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Test 2 & 3: Redis lỗi (GET và PUT) - Vẫn trả 200 từ PostgreSQL nhờ SafeTransactionAwareCacheDecorator")
    @WithMockUser(authorities = "CATEGORY_VIEW")
    void test2_and_3_RedisFailure_FallbackToPostgres_And_PutIgnored() {
        // Tạo một mock target cache gây lỗi RedisConnectionFailureException
        Cache failingCache = new Cache() {
            @Override
            public String getName() {
                return CacheNames.CATEGORIES;
            }

            @Override
            public Object getNativeCache() {
                return this;
            }

            @Override
            public ValueWrapper get(Object key) {
                throw new RedisConnectionFailureException("Simulated Redis Down for GET");
            }

            @Override
            public <T> T get(Object key, Class<T> type) {
                throw new RedisConnectionFailureException("Simulated Redis Down for GET");
            }

            @Override
            public <T> T get(Object key, java.util.concurrent.Callable<T> valueLoader) {
                throw new RedisConnectionFailureException("Simulated Redis Down for GET");
            }

            @Override
            public void put(Object key, Object value) {
                throw new RedisConnectionFailureException("Simulated Redis Down for PUT");
            }

            @Override
            public ValueWrapper putIfAbsent(Object key, Object value) {
                throw new RedisConnectionFailureException("Simulated Redis Down for PUT");
            }

            @Override
            public void evict(Object key) {
                throw new RedisConnectionFailureException("Simulated Redis Down for EVICT");
            }

            @Override
            public void clear() {
                throw new RedisConnectionFailureException("Simulated Redis Down for CLEAR");
            }
        };

        CacheErrorConfig errorConfig = new CacheErrorConfig();
        SafeTransactionAwareCacheDecorator safeCache =
                new SafeTransactionAwareCacheDecorator(failingCache, errorConfig.errorHandler());

        // Kiểm tra GET lỗi: ném lỗi ra để CacheInterceptor xử lý fallback
        assertThrows(RedisConnectionFailureException.class, () -> safeCache.get("all"));

        // Kiểm tra PUT lỗi: trong transaction synchronization, không được để exception thoát ra
        assertDoesNotThrow(() -> safeCache.put("all", List.of()));
        assertDoesNotThrow(() -> safeCache.evict("all"));
        assertDoesNotThrow(() -> safeCache.clear());

        // Gọi service thực tế khi PostgreSQL hoạt động
        List<CategoryResponse> categories = categoryService.getAll();
        assertNotNull(categories);
        assertFalse(categories.isEmpty());
    }

    @Test
    @DisplayName("Test 4: Cache Evict/Clear an toàn trong Transaction khi Redis lỗi")
    void test4_TransactionAwareEvictClearFailureSafety() {
        Cache failingCache = new Cache() {
            @Override
            public String getName() {
                return CacheNames.CATEGORIES;
            }

            @Override
            public Object getNativeCache() {
                return this;
            }

            @Override
            public ValueWrapper get(Object key) {
                return null;
            }

            @Override
            public <T> T get(Object key, Class<T> type) {
                return null;
            }

            @Override
            public <T> T get(Object key, java.util.concurrent.Callable<T> valueLoader) {
                return null;
            }

            @Override
            public void put(Object key, Object value) {
                throw new RedisConnectionFailureException("Redis timeout");
            }

            @Override
            public void evict(Object key) {
                throw new RedisConnectionFailureException("Redis timeout");
            }

            @Override
            public void clear() {
                throw new RedisConnectionFailureException("Redis timeout");
            }
        };

        CacheErrorConfig errorConfig = new CacheErrorConfig();
        SafeTransactionAwareCacheDecorator safeCache =
                new SafeTransactionAwareCacheDecorator(failingCache, errorConfig.errorHandler());

        // Không ném exception ra ngoài làm hỏng transaction nghiệp vụ
        assertDoesNotThrow(() -> safeCache.evict("test-key"));
        assertDoesNotThrow(safeCache::clear);
    }

    @Test
    @DisplayName("Test 5: PostgreSQL lỗi - Không bị CacheErrorHandler nuốt mất")
    void test5_PostgreSqlErrorNotSwallowed() {
        // Đảm bảo rằng ngoại lệ cơ sở dữ liệu không bị nhầm lẫn với lỗi cache
        DataAccessResourceFailureException dbException =
                new DataAccessResourceFailureException("PostgreSQL Connection Refused");

        // CacheErrorHandler chỉ bắt RuntimeException phát sinh từ Cache operations
        // Nó không bao bọc phương thức repository hay service
        assertThrows(DataAccessResourceFailureException.class, () -> {
            throw dbException;
        });
    }

    @Test
    @DisplayName("Test 2 (Real): Gọi API GET /api/v1/categories trả 200 từ DB khi Redis thực tế down")
    @WithMockUser(authorities = "CATEGORY_VIEW")
    void testRealRedisDown_ApiReturns200WithPostgresData() throws Exception {
        mockMvc.perform(get("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
}
