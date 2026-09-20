package com.gmoon.cacheinvalidation.core.config;

import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

import com.gmoon.cacheinvalidation.core.config.CacheFailureRecorder.Operation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class FallbackCacheErrorHandler implements CacheErrorHandler {

	private final CacheFailureRecorder recorder;

	@Override
	public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
		degradeToOrigin(Operation.GET, exception, cache, key);
	}

	@Override
	public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
		degradeToOrigin(Operation.PUT, exception, cache, key);
	}

	@Override
	public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
		degradeToOrigin(Operation.EVICT, exception, cache, key);
	}

	@Override
	public void handleCacheClearError(RuntimeException exception, Cache cache) {
		degradeToOrigin(Operation.CLEAR, exception, cache, null);
	}

	private void degradeToOrigin(Operation operation, RuntimeException exception, Cache cache, Object key) {
		recorder.record(operation);
		log.warn("cache {} failed. falling back to origin. cache: {}, key: {}",
			 operation, cache.getName(), key, exception);
	}
}
