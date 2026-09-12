package com.gmoon.cacheinvalidation.core.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.gmoon.cacheinvalidation.core.cache.expiration.TtlResolver;
import com.gmoon.cacheinvalidation.core.cache.policy.CachePolicy;
import com.gmoon.cacheinvalidation.core.cache.policy.CachePolicyRegistry;
import com.gmoon.cacheinvalidation.core.cache.serialization.SerializerFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RedisCacheSettings {

	private static final Duration FALLBACK_TTL = Duration.ofMinutes(5);

	private final SerializerFactory serializerFactory;
	private final TtlResolver ttlResolver;
	private final CacheProperties.Redis redis;

	public RedisCacheConfiguration unregisteredCacheDefaults() {
		return configurationOf(defaultTtl(), serializerFactory.unregisteredCacheSerializer());
	}

	public Map<String, RedisCacheConfiguration> byCacheName(CachePolicyRegistry registry) {
		Map<String, RedisCacheConfiguration> byCacheName = new HashMap<>();
		for (CachePolicy policy : registry.all()) {
			byCacheName.put(policy.cacheName(),
				 configurationOf(policy.ttl(), serializerFactory.valueSerializerFor(policy)));
		}
		return byCacheName;
	}

	private Duration defaultTtl() {
		return redis.getTimeToLive() == null ? FALLBACK_TTL : redis.getTimeToLive();
	}

	private RedisCacheConfiguration configurationOf(Duration baseTtl, RedisSerializer<?> valueSerializer) {
		RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
			 .entryTtl(ttlResolver.resolveFrom(baseTtl))
			 .serializeKeysWith(
				  RedisSerializationContext.SerializationPair.fromSerializer(serializerFactory.keySerializer()))
			 .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer));

		return applyKeyPrefix(configuration);
	}

	private RedisCacheConfiguration applyKeyPrefix(RedisCacheConfiguration configuration) {
		if (!redis.isUseKeyPrefix()) {
			return configuration.disableKeyPrefix();
		}
		return redis.getKeyPrefix() == null
			 ? configuration
			 : configuration.prefixCacheNameWith(redis.getKeyPrefix());
	}
}
