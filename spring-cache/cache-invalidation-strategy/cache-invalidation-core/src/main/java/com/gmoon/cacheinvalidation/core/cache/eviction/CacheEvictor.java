package com.gmoon.cacheinvalidation.core.cache.eviction;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 캐시에서 항목을 지우고 결과만 돌려준다.
 * <p>
 * 실패를 어디에 기록할지는 호출자가 정한다. 여기서 함께 기록하면 같은 실패가 두 곳에 쌓인다.
 */
@Slf4j
@RequiredArgsConstructor
public class CacheEvictor {

	private final CacheManager cacheManager;

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
			log.warn("cache eviction failed. entry: {}", entry, e);
			return EvictionOutcome.FAILED;
		}
	}
}
