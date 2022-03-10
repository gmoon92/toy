package com.gmoon.springdataredis.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.redis.cache.RedisCacheConfiguration;

import com.gmoon.springdataredis.redis.CacheName;
import com.gmoon.springdataredis.util.RedisUtils;

import lombok.Getter;

@Getter
public class CacheExpireConfigs {
	private final Map<String, RedisCacheConfiguration> values;

	private CacheExpireConfigs() {
		this.values = new HashMap<>();
	}

	public static CacheExpireConfigs create() {
		return new CacheExpireConfigs();
	}

	public CacheExpireConfigs putExpireMinutes(CacheName cacheName, int minutes) {
		Duration expireTtl = Duration.ofMinutes(minutes);
		values.put(cacheName.getValue(), RedisUtils.createRedisCacheConfig(expireTtl));
		return this;
	}
}
