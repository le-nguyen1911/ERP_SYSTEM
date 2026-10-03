package com.ERP_SYSTEM.auth;

import com.ERP_SYSTEM.auth.config.RateLimitConfig;
import com.ERP_SYSTEM.auth.config.RateLimitFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RateLimitFilterTest {

    private RateLimitFilter rateLimitFilter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        rateLimitFilter = new RateLimitFilter(objectMapper);
        rateLimitFilter.clearBucketsForTesting();
    }

    @Test
    @DisplayName("Should allow up to 5 login requests and reject 6th request with HTTP 429")
    void loginRateLimiting_Allows5Requests_ThenReturns429() throws ServletException, IOException {
        String clientIp = "192.168.1.100";

        for (int i = 1; i <= 5; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
            request.setRemoteAddr(clientIp);
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain filterChain = mock(FilterChain.class);

            rateLimitFilter.doFilter(request, response, filterChain);

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getHeader("X-RateLimit-Limit")).isEqualTo("5");
            assertThat(response.getHeader("X-RateLimit-Remaining")).isEqualTo(String.valueOf(5 - i));
            verify(filterChain, times(1)).doFilter(request, response);
        }

        // 6th request should be blocked with 429
        MockHttpServletRequest blockedRequest = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        blockedRequest.setRemoteAddr(clientIp);
        MockHttpServletResponse blockedResponse = new MockHttpServletResponse();
        FilterChain blockedChain = mock(FilterChain.class);

        rateLimitFilter.doFilter(blockedRequest, blockedResponse, blockedChain);

        verify(blockedChain, never()).doFilter(blockedRequest, blockedResponse);
        assertThat(blockedResponse.getStatus()).isEqualTo(429);
        assertThat(blockedResponse.getHeader("X-RateLimit-Limit")).isEqualTo("5");
        assertThat(blockedResponse.getHeader("X-RateLimit-Remaining")).isEqualTo("0");
        assertThat(blockedResponse.getHeader("Retry-After")).isNotNull();
        assertThat(Long.parseLong(blockedResponse.getHeader("Retry-After"))).isGreaterThan(0);

        // Verify response body matches ApiResponse
        JsonNode jsonNode = objectMapper.readTree(blockedResponse.getContentAsString());
        assertThat(jsonNode.get("success").asBoolean()).isFalse();
        assertThat(jsonNode.get("message").asText()).isEqualTo("Quá nhiều yêu cầu. Vui lòng thử lại sau.");
        assertThat(jsonNode.get("error").asText()).isEqualTo("RATE_LIMIT_EXCEEDED");
        assertThat(jsonNode.hasNonNull("timestamp")).isTrue();
    }

    @Test
    @DisplayName("Should use separate buckets for different client IPs")
    void differentClientIps_HaveIsolatedBuckets() throws ServletException, IOException {
        String ip1 = "10.0.0.1";
        String ip2 = "10.0.0.2";

        // Exhaust IP 1
        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/auth/login");
            req.setRemoteAddr(ip1);
            MockHttpServletResponse res = new MockHttpServletResponse();
            rateLimitFilter.doFilter(req, res, new MockFilterChain());
        }

        // IP 1 is blocked
        MockHttpServletRequest req1 = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        req1.setRemoteAddr(ip1);
        MockHttpServletResponse res1 = new MockHttpServletResponse();
        rateLimitFilter.doFilter(req1, res1, new MockFilterChain());
        assertThat(res1.getStatus()).isEqualTo(429);

        // IP 2 is unaffected and succeeds
        MockHttpServletRequest req2 = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        req2.setRemoteAddr(ip2);
        MockHttpServletResponse res2 = new MockHttpServletResponse();
        MockFilterChain chain2 = new MockFilterChain();
        rateLimitFilter.doFilter(req2, res2, chain2);

        assertThat(res2.getStatus()).isEqualTo(200);
        assertThat(res2.getHeader("X-RateLimit-Remaining")).isEqualTo("4");
    }

    @Test
    @DisplayName("Should correctly extract client IP from X-Forwarded-For header")
    void extractClientIp_WithXForwardedFor() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.addHeader("X-Forwarded-For", "203.0.113.195, 70.41.3.18, 150.172.238.178");
        request.setRemoteAddr("127.0.0.1");

        MockHttpServletResponse response = new MockHttpServletResponse();
        rateLimitFilter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader("X-RateLimit-Remaining")).isEqualTo("4");
    }

    @Test
    @DisplayName("Should support refresh token endpoints with capacity 10 separately from login")
    void refreshTokenRateLimit_SeparateBucketCapacity10() throws ServletException, IOException {
        String clientIp = "172.16.0.5";

        // Consume 5 login tokens
        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/auth/login");
            req.setRemoteAddr(clientIp);
            MockHttpServletResponse res = new MockHttpServletResponse();
            rateLimitFilter.doFilter(req, res, new MockFilterChain());
        }

        // Login is now exhausted (next returns 429)
        MockHttpServletRequest loginReq = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        loginReq.setRemoteAddr(clientIp);
        MockHttpServletResponse loginRes = new MockHttpServletResponse();
        rateLimitFilter.doFilter(loginReq, loginRes, new MockFilterChain());
        assertThat(loginRes.getStatus()).isEqualTo(429);

        // Refresh token endpoint /api/v1/auth/refresh-token has its own bucket of capacity 10 and still succeeds!
        for (int i = 1; i <= 10; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/auth/refresh-token");
            req.setRemoteAddr(clientIp);
            MockHttpServletResponse res = new MockHttpServletResponse();
            rateLimitFilter.doFilter(req, res, new MockFilterChain());

            assertThat(res.getStatus()).isEqualTo(200);
            assertThat(res.getHeader("X-RateLimit-Limit")).isEqualTo("10");
            assertThat(res.getHeader("X-RateLimit-Remaining")).isEqualTo(String.valueOf(10 - i));
        }

        // 11th refresh request returns 429
        MockHttpServletRequest req11 = new MockHttpServletRequest("POST", "/api/v1/auth/refresh-token");
        req11.setRemoteAddr(clientIp);
        MockHttpServletResponse res11 = new MockHttpServletResponse();
        rateLimitFilter.doFilter(req11, res11, new MockFilterChain());
        assertThat(res11.getStatus()).isEqualTo(429);
    }

    @Test
    @DisplayName("Should rate limit register endpoint with capacity 3")
    void registerRateLimit_Capacity3() throws ServletException, IOException {
        String clientIp = "192.168.1.50";

        for (int i = 1; i <= 3; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/auth/register");
            req.setRemoteAddr(clientIp);
            MockHttpServletResponse res = new MockHttpServletResponse();
            rateLimitFilter.doFilter(req, res, new MockFilterChain());

            assertThat(res.getStatus()).isEqualTo(200);
            assertThat(res.getHeader("X-RateLimit-Limit")).isEqualTo("3");
            assertThat(res.getHeader("X-RateLimit-Remaining")).isEqualTo(String.valueOf(3 - i));
        }

        // 4th register request blocked
        MockHttpServletRequest req4 = new MockHttpServletRequest("POST", "/api/v1/auth/register");
        req4.setRemoteAddr(clientIp);
        MockHttpServletResponse res4 = new MockHttpServletResponse();
        rateLimitFilter.doFilter(req4, res4, new MockFilterChain());
        assertThat(res4.getStatus()).isEqualTo(429);
    }

    @Test
    @DisplayName("Should bypass rate limiting for GET methods or non-auth endpoints")
    void nonRateLimitedEndpoints_BypassedImmediately() throws ServletException, IOException {
        // GET /api/v1/auth/login should not be rate limited
        MockHttpServletRequest getAuthReq = new MockHttpServletRequest("GET", "/api/v1/auth/login");
        MockHttpServletResponse getAuthRes = new MockHttpServletResponse();
        FilterChain chain1 = mock(FilterChain.class);
        rateLimitFilter.doFilter(getAuthReq, getAuthRes, chain1);
        verify(chain1, times(1)).doFilter(getAuthReq, getAuthRes);
        assertThat(getAuthRes.getHeader("X-RateLimit-Limit")).isNull();

        // GET /api/v1/auth/me should not be rate limited
        MockHttpServletRequest getMeReq = new MockHttpServletRequest("GET", "/api/v1/auth/me");
        MockHttpServletResponse getMeRes = new MockHttpServletResponse();
        FilterChain chain2 = mock(FilterChain.class);
        rateLimitFilter.doFilter(getMeReq, getMeRes, chain2);
        verify(chain2, times(1)).doFilter(getMeReq, getMeRes);
        assertThat(getMeRes.getHeader("X-RateLimit-Limit")).isNull();

        // Business API e.g. GET /api/v1/products should not be touched
        MockHttpServletRequest businessReq = new MockHttpServletRequest("GET", "/api/v1/products");
        MockHttpServletResponse businessRes = new MockHttpServletResponse();
        FilterChain chain3 = mock(FilterChain.class);
        rateLimitFilter.doFilter(businessReq, businessRes, chain3);
        verify(chain3, times(1)).doFilter(businessReq, businessRes);
        assertThat(businessRes.getHeader("X-RateLimit-Limit")).isNull();
    }

    @Test
    @DisplayName("Should handle servlet context path correctly")
    void contextPath_HandledCorrectly() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/erp/api/v1/auth/login");
        request.setContextPath("/erp");
        request.setRemoteAddr("10.10.10.10");
        MockHttpServletResponse response = new MockHttpServletResponse();

        rateLimitFilter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader("X-RateLimit-Limit")).isEqualTo("5");
        assertThat(response.getHeader("X-RateLimit-Remaining")).isEqualTo("4");
    }
}
