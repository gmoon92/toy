package com.gmoon.springlockredisson.config;

import java.io.IOException;
import java.time.Duration;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import lombok.extern.slf4j.Slf4j;
import redis.embedded.RedisServer;

/**
 * [ ] https://github.com/ozimov/embedded-redis
 * [X] https://github.com/kstyrc/embedded-redis
 * */
@Slf4j
@TestConfiguration
public class EmbeddedRedisConfig {

	@Bean(initMethod = "start", destroyMethod = "stop")
	public RedisServer redisServer(RedisProperties redisProperties) throws IOException {
		return new RedisServer(redisProperties.getPort());
	}

	@Bean
	@Primary
	public RedissonClient embeddedRedissonClient(RedisProperties properties, RedisServer redisServer) {
		String host = properties.getHost();
		int port = properties.getPort();
		Duration connectTimeout = properties.getConnectTimeout();

		Config config = new Config();
		config.useSingleServer()
			.setAddress(String.format("redis://%s:%d", host, port))
			.setTimeout((int)connectTimeout.toMillis())
			.setUsername(properties.getUsername())
			.setPassword(properties.getPassword());
		return Redisson.create(config);
	}
}
