package com.example.Spot.global.infrastructure.config.redis;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        // 1. 날짜 처리를 위한 ObjectMapper 설정
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // 날짜 모듈 등록
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ISO-8601 형식으로 저장

        // 보안을 위한 타입 검증기 설정 (JSON에 클래스 정보를 포함시키기 위함)
        BasicPolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();
        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);

        // 2. 위 설정을 가진 Serializer 생성
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // 3. 캐시 설정에 적용
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer)) // 커스텀 시리얼라이저 적용
            .entryTtl(Duration.ofMinutes(10));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
}