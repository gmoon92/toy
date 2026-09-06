package com.gmoon.cacheinvalidation.core.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import com.gmoon.cacheinvalidation.core.resilience.CacheFailureRecorder;
import com.gmoon.cacheinvalidation.core.resilience.CacheOperation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CacheEvictor {

	private final CacheManager cacheManager;
	private final CacheFailureRecorder failureRecorder;

	public void evict(CacheEntryRef entry) {
		Cache cache = cacheManager.getCache(entry.cacheName());
		if (cache == null) {
			log.warn("cache not found. name: {}", entry.cacheName());
			return;
		}

		try {
			cache.evictIfPresent(entry.key());
		} catch (RuntimeException e) {
			failureRecorder.record(CacheOperation.EVICT);
			log.warn("cache eviction failed. entry: {}", entry, e);
		}
	}
}
