package com.gmoon.springdataredis.config;

import java.time.Duration;

import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RedisUtils {
	public RedisCacheConfiguration createRedisCacheConfig(Duration expireTtl) {
		return RedisCacheConfiguration.defaultCacheConfig()
			.entryTtl(expireTtl)
			.disableCachingNullValues()
			.prefixCacheNameWith(CacheKeyPrefix.SEPARATOR)
			.computePrefixWith(CacheKeyPrefix.simple())
			.serializeKeysWith(RedisSerializationContext
				.SerializationPair
				.fromSerializer(getKeySerializer()));
	}

	public StringRedisSerializer getKeySerializer() {
		return StringRedisSerializer.UTF_8;
	}
}
