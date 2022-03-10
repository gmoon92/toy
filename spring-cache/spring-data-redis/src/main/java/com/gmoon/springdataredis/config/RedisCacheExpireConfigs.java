package com.gmoon.springdataredis.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.redis.cache.RedisCacheConfiguration;

import com.gmoon.springdataredis.cache.CacheName;
import com.gmoon.springdataredis.util.RedisUtils;

import lombok.Getter;

@Getter
public class RedisCacheExpireConfigs {
	private final Map<String, RedisCacheConfiguration> values;

	private RedisCacheExpireConfigs() {
		this.values = new HashMap<>();
	}

	public static RedisCacheExpireConfigs create() {
		return new RedisCacheExpireConfigs();
	}

	public RedisCacheExpireConfigs putExpireMinutes(CacheName cacheName, int minutes) {
		Duration expireTtl = Duration.ofMinutes(minutes);
		values.put(cacheName.getValue(), RedisUtils.createRedisCacheConfig(expireTtl));
		return this;
	}
}
