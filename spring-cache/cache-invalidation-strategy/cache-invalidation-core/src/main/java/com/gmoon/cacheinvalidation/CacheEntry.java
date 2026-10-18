package com.gmoon.cacheinvalidation;

import org.springframework.util.Assert;

public record CacheEntry(CachePolicy policy, String key) {

	public CacheEntry {
		Assert.notNull(policy, "cache policy must not be null");
		Assert.hasText(key, "cache key must not be empty");
	}

	public static CacheEntry of(CachePolicy policy, Object key) {
		return new CacheEntry(policy, String.valueOf(key));
	}

	public String cacheName() {
		return policy.cacheName();
	}
}
