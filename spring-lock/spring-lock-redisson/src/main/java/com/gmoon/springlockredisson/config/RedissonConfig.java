package com.gmoon.springlockredisson.config;

import java.time.Duration;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gmoon.javacore.util.StringUtils;

@Configuration
public class RedissonConfig {

	@Bean
	public RedissonClient redissonClient(RedisProperties properties) {
		Config config = getConfig(properties);

		return Redisson.create(config);
	}

	private Config getConfig(RedisProperties properties) {
		String host = properties.getHost();
		int port = properties.getPort();
		Duration connectTimeout = properties.getConnectTimeout();

		Config config = new Config();

		SingleServerConfig singleServerConfig = config.useSingleServer()
			.setAddress(String.format("redis://%s:%d", host, port))
			.setTimeout((int)connectTimeout.toMillis())
			.setUsername(properties.getUsername());

		String password = properties.getPassword();
		if (StringUtils.isNotBlank(password)) {
			singleServerConfig.setPassword(properties.getPassword());
		}
		return config;
	}
}
