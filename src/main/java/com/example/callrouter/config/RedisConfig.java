package com.example.callrouter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import com.example.callrouter.model.UserRegistration;

@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, UserRegistration> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, UserRegistration> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(UserRegistration.class));
        return template;
    }
}