package com.gmoon.cacheinvalidation.config;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;

@DisplayName("캐시 장애 처리")
class FallbackCacheErrorHandlerTest {

	private static final String CACHE_NAME = "USER";
	private static final RuntimeException REDIS_DOWN = new IllegalStateException("connection refused");

	private CacheErrorHandler handler;
	private Cache cache;

	@BeforeEach
	void setUp() {
		handler = new FallbackCacheErrorHandler();
		cache = mock(Cache.class);
		when(cache.getName()).thenReturn(CACHE_NAME);
	}

	@Nested
	@DisplayName("스프링 기본 처리기는")
	class DefaultHandler {

		@Test
		@DisplayName("캐시 조회 실패를 그대로 전파해 요청을 실패시킨다")
		void rethrowsAndFailsRequest() {
			CacheErrorHandler defaultHandler = new SimpleCacheErrorHandler();

			assertThatThrownBy(() -> defaultHandler.handleCacheGetError(REDIS_DOWN, cache, 1L))
				 .as("기본 설정에서는 Redis 장애가 곧 서비스 장애가 된다")
				 .isSameAs(REDIS_DOWN);
		}
	}

	@Nested
	@DisplayName("폴백 처리기는")
	class FallbackHandler {

		@Test
		@DisplayName("조회 실패를 삼켜 원본 조회로 넘어가게 한다")
		void swallowsGetSoOriginIsQueried() {
			assertThatNoException()
				 .as("조회 실패가 전파되면 캐시가 단일 장애점이 된다")
				 .isThrownBy(() -> handler.handleCacheGetError(REDIS_DOWN, cache, 1L));
		}

		@Test
		@DisplayName("적재 실패를 삼켜 응답에 영향을 주지 않는다")
		void swallowsPutWithoutAffectingResponse() {
			assertThatNoException()
				 .isThrownBy(() -> handler.handleCachePutError(REDIS_DOWN, cache, 1L, "value"));
		}

		@Test
		@DisplayName("무효화 실패를 삼킨다")
		void swallowsEvict() {
			assertThatNoException()
				 .as("무효화 유실은 stale 을 남기지만, 예외를 전파하면 요청 자체가 깨진다")
				 .isThrownBy(() -> handler.handleCacheEvictError(REDIS_DOWN, cache, 1L));
		}

		@Test
		@DisplayName("전체 삭제 실패를 삼킨다")
		void swallowsClear() {
			assertThatNoException()
				 .isThrownBy(() -> handler.handleCacheClearError(REDIS_DOWN, cache));
		}
	}
}
