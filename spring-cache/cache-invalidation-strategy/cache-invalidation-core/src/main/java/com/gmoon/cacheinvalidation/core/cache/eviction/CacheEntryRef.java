package com.gmoon.cacheinvalidation.core.cache.eviction;

import java.time.Duration;

import org.springframework.util.Assert;
import com.gmoon.cacheinvalidation.core.cache.policy.CachePolicy;

public record CacheEntryRef(CachePolicy policy, String key) {

	public CacheEntryRef {
		Assert.notNull(policy, "cache policy must not be null");
		Assert.hasText(key, "cache key must not be empty");
	}

	public static CacheEntryRef of(CachePolicy policy, Object key) {
		return new CacheEntryRef(policy, String.valueOf(key));
	}

	public String cacheName() {
		return policy.cacheName();
	}

	public Duration ttl() {
		return policy.ttl();
	}
}
