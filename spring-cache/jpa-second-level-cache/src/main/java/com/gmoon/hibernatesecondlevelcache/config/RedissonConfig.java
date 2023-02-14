package com.gmoon.hibernatesecondlevelcache.config;

import java.time.Duration;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RedissonConfig {

	@Bean
	@ConditionalOnProperty(name = "cache.storage", havingValue = "REDIS")
	public RedissonClient redissonClient(RedisProperties properties) {
		Config config = new Config();
		SingleServerConfig singleServerConfig = config.useSingleServer();

		String password = properties.getPassword();
		if (StringUtils.isNotBlank(password)) {
			singleServerConfig.setPassword(password);
		}

		Duration connectTimeout = properties.getConnectTimeout();
		config.useSingleServer()
			.setAddress(properties.getUrl())
			.setTimeout((int)connectTimeout.toMillis());
		return Redisson.create(config);
	}

}

