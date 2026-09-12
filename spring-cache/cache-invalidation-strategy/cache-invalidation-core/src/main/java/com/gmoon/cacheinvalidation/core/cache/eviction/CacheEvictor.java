package com.gmoon.cacheinvalidation.core.cache.eviction;

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

	public EvictionOutcome evict(CacheEntryRef entry) {
		try {
			Cache cache = cacheManager.getCache(entry.cacheName());
			if (cache == null) {
				log.warn("cache not registered. name: {}", entry.cacheName());
				return EvictionOutcome.CACHE_NOT_REGISTERED;
			}
			cache.evict(entry.key());
			return EvictionOutcome.EVICT_REQUESTED;
		} catch (RuntimeException e) {
			failureRecorder.record(CacheOperation.EVICT);
			log.warn("cache eviction failed. entry: {}", entry, e);
			return EvictionOutcome.FAILED;
		}
	}
}
