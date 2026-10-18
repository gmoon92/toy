package com.gmoon.cacheinvalidation.config;

import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * 캐시 접근이 실패해도 원본 조회로 넘어가게 한다.
 * <p>
 * 스프링 기본 처리기({@code SimpleCacheErrorHandler})는 예외를 그대로 전파해
 * Redis 장애가 곧 서비스 장애가 된다.
 */
@Slf4j
class FallbackCacheErrorHandler implements CacheErrorHandler {

	@Override
	public void handleCacheGetError(
		 RuntimeException exception,
		 Cache cache,
		 Object key
	) {
		degradeToOrigin(
			 "get",
			 exception,
			 cache,
			 key
		);
	}

	@Override
	public void handleCachePutError(
		 RuntimeException exception,
		 Cache cache,
		 Object key,
		 Object value
	) {
		degradeToOrigin(
			 "put",
			 exception,
			 cache,
			 key
		);
	}

	@Override
	public void handleCacheEvictError(
		 RuntimeException exception,
		 Cache cache,
		 Object key
	) {
		degradeToOrigin(
			 "evict",
			 exception,
			 cache,
			 key
		);
	}

	@Override
	public void handleCacheClearError(
		 RuntimeException exception,
		 Cache cache
	) {
		degradeToOrigin(
			 "clear",
			 exception,
			 cache,
			 null
		);
	}

	private void degradeToOrigin(
		 String operation,
		 RuntimeException exception,
		 Cache cache,
		 Object key
	) {
		log.warn(
			 "cache {} failed. falling back to origin. cache: {}, key: {}",
			 operation,
			 cache.getName(),
			 key,
			 exception
		);
	}
}
