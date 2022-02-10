package com.gmoon.springdataredis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * RedisTemplate default key serializer is {@link org.springframework.data.redis.serializer.JdkSerializationRedisSerializer}
 * apply all serializers to key, value, hash key, hash value.
 * {@link org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration}
 * @see RedisTemplate#afterPropertiesSet()
 * **/
@Slf4j
@Configuration
public class RedisConfig {
	@Bean
	public StringRedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
		log.info("connectionFactory: {}", connectionFactory);
		return new StringRedisTemplate(connectionFactory);
	}
}
