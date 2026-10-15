package com.ERP_SYSTEM.common.redis;

import com.ERP_SYSTEM.common.cache.CacheNames;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public GenericJackson2JsonRedisSerializer redisValueSerializer() {
        return new GenericJackson2JsonRedisSerializer(cacheObjectMapper());
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(
            @org.springframework.beans.factory.annotation.Value("${spring.data.redis.host:localhost}") String host,
            @org.springframework.beans.factory.annotation.Value("${spring.data.redis.port:6379}") int port,
            @org.springframework.beans.factory.annotation.Value("${spring.data.redis.password:}") String password) {

        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(host);
        redisStandaloneConfiguration.setPort(port);
        if (password != null && !password.isBlank()) {
            redisStandaloneConfiguration.setPassword(password);
        }

        // Cấu hình Socket options & Command Timeout cho Lettuce
        SocketOptions socketOptions = SocketOptions.builder()
                .connectTimeout(Duration.ofMillis(1000))
                .build();

        ClientOptions clientOptions = ClientOptions.builder()
                .socketOptions(socketOptions)
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .build();

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(1000))
                .clientOptions(clientOptions)
                .build();

        return new LettuceConnectionFactory(redisStandaloneConfiguration, clientConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            GenericJackson2JsonRedisSerializer redisValueSerializer) {

        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(redisValueSerializer);

        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(redisValueSerializer);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    public RedisCacheManager redisCacheManager(
            RedisConnectionFactory connectionFactory,
            GenericJackson2JsonRedisSerializer redisValueSerializer) {

        RedisSerializationContext.SerializationPair<Object> valueSerializationPair =
                RedisSerializationContext.SerializationPair.fromSerializer(redisValueSerializer);

        RedisCacheConfiguration defaultConfig =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(10))
                        .disableCachingNullValues()
                        .serializeKeysWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(new StringRedisSerializer())
                        )
                        .serializeValuesWith(valueSerializationPair);

        Map<String, RedisCacheConfiguration> cacheConfigurationMap = new HashMap<>();
        cacheConfigurationMap.put(CacheNames.CATEGORIES, defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurationMap.put(CacheNames.UNITS, defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurationMap.put(CacheNames.WAREHOUSES, defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurationMap.put(CacheNames.PRODUCTS, defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurationMap.put(CacheNames.SUPPLIERS, defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurationMap.put(CacheNames.CUSTOMERS, defaultConfig.entryTtl(Duration.ofMinutes(15)));

        RedisCacheWriter cacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory);

        RedisCacheManager cacheManager = new RedisCacheManager(
                cacheWriter,
                defaultConfig,
                cacheConfigurationMap
        );
        cacheManager.setTransactionAware(false);
        return cacheManager;
    }

    private ObjectMapper cacheObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        PolymorphicTypeValidator validator =
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType("com.ERP_SYSTEM")
                        .allowIfSubType("java.util")
                        .allowIfSubType("java.lang")
                        .allowIfSubType("java.math")
                        .allowIfSubType("java.time")
                        .build();

        mapper.activateDefaultTyping(
                validator,
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
        );

        return mapper;
    }
}