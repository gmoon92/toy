package com.gmoon.cacheinvalidation;

import java.util.List;

import org.springframework.cache.CacheManager;

public interface CacheInvalidator {

	static CacheInvalidator of(List<InvalidationRule> rules, CacheManager cacheManager) {
		return new DefaultCacheInvalidator(rules, cacheManager);
	}

	void invalidate(EntityChange change);
}
