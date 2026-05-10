package com.internship.tool.config;
 
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.*;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.*;
 
import java.time.Duration;
import java.util.Map;
 
@Configuration
public class RedisConfig {
 
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        var jsonSerializer = new GenericJackson2JsonRedisSerializer();
 
        var defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer)
            )
            .disableCachingNullValues();
 
        return RedisCacheManager.builder(factory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(Map.of(
                "policies",    defaultConfig.entryTtl(Duration.ofMinutes(10)),
                "stats",       defaultConfig.entryTtl(Duration.ofMinutes(5)),
                "auditLogs",   defaultConfig.entryTtl(Duration.ofMinutes(2))
            ))
            .build();
    }
}
 