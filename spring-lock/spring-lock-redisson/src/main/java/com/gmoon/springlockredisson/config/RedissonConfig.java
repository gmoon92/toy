package com.gmoon.springlockredisson.config;

import java.time.Duration;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

	@Bean
	public RedissonClient redissonClient(RedisProperties properties) {
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
