package com.gmoon.cacheinvalidation.core.invalidation;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CacheEvictor {

	private final CacheManager cacheManager;

	public EvictionOutcome evict(CacheKey entry) {
		try {
			Cache cache = cacheManager.getCache(entry.cacheName());
			if (cache == null) {
				log.warn("cache not registered. name: {}", entry.cacheName());
				return EvictionOutcome.CACHE_NOT_REGISTERED;
			}
			cache.evict(entry.key());
			return EvictionOutcome.EVICT_REQUESTED;
		} catch (RuntimeException e) {
			log.warn("cache eviction failed. entry: {}", entry, e);
			return EvictionOutcome.FAILED;
		}
	}
}
