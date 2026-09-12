package com.gmoon.cacheinvalidation.core.cache;

import java.time.Duration;

import org.springframework.util.Assert;

public record CacheSpec(String cacheName, Duration ttl, Class<?> valueType, InvalidationMode invalidationMode) {

	public CacheSpec {
		Assert.hasText(cacheName, "cache name must not be empty");
		Assert.notNull(ttl, "ttl must not be null");
		Assert.isTrue(!ttl.isNegative() && !ttl.isZero(), "ttl must be positive");
		Assert.notNull(valueType, "value type must not be null");
		Assert.notNull(invalidationMode, "invalidation mode must not be null");
	}

	public static CacheSpec of(String cacheName, Duration ttl, Class<?> valueType) {
		return new CacheSpec(cacheName, ttl, valueType, InvalidationMode.RULE);
	}

	public CacheSpec invalidatedByTtlOnly() {
		return new CacheSpec(cacheName, ttl, valueType, InvalidationMode.TTL_ONLY);
	}
}
