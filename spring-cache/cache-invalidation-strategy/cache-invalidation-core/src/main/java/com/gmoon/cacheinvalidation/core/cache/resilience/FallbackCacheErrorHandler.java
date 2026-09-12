package com.gmoon.cacheinvalidation.core.cache.resilience;

import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class FallbackCacheErrorHandler implements CacheErrorHandler {

	private final CacheFailureRecorder recorder;

	@Override
	public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
		degradeToOrigin(CacheOperation.GET, exception, cache, key);
	}

	@Override
	public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
		degradeToOrigin(CacheOperation.PUT, exception, cache, key);
	}

	@Override
	public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
		degradeToOrigin(CacheOperation.EVICT, exception, cache, key);
	}

	@Override
	public void handleCacheClearError(RuntimeException exception, Cache cache) {
		degradeToOrigin(CacheOperation.CLEAR, exception, cache, null);
	}

	private void degradeToOrigin(CacheOperation operation, RuntimeException exception, Cache cache, Object key) {
		recorder.record(operation);
		log.warn("cache {} failed. falling back to origin. cache: {}, key: {}",
			 operation, cache.getName(), key, exception);
	}
}
