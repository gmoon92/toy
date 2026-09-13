package com.gmoon.cacheinvalidation.core.invalidation;

import org.springframework.util.Assert;

import com.gmoon.cacheinvalidation.core.policy.CachePolicy;

public record CacheKey(CachePolicy policy, String key) {

	public CacheKey {
		Assert.notNull(policy, "cache policy must not be null");
		Assert.hasText(key, "cache key must not be empty");
	}

	public static CacheKey of(CachePolicy policy, Object key) {
		return new CacheKey(policy, String.valueOf(key));
	}

	public String cacheName() {
		return policy.cacheName();
	}
}
