package com.gmoon.hibernatesecondlevelcache.config;

import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RedissonConfig {

	@Bean
	@ConditionalOnProperty(name = "cache.storage", havingValue = "REDIS")
	public RedissonClient redissonClient(RedisProperties properties) {
		Config config = getServerConfig(properties);
		return Redisson.create(config);
	}

	private Config getServerConfig(RedisProperties properties) {
		Config config = new Config();
		config.useSingleServer()
			.setPassword(getPassword(properties))
			.setAddress(properties.getUrl())
			.setAddress(String.format("redis://%s:%d", properties.getHost(), properties.getPort()))
			.setTimeout((int)properties.getConnectTimeout().toMillis());
		return config;
	}

	private String getPassword(RedisProperties properties) {
		String password = properties.getPassword();
		if (StringUtils.isBlank(password)) {
			return null;
		}
		return password;
	}
}

