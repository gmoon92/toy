package com.gmoon.cacheinvalidation.core.cache.resilience;

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

	private CacheFailureRecorder recorder;
	private CacheErrorHandler handler;
	private Cache cache;

	@BeforeEach
	void setUp() {
		recorder = new CacheFailureRecorder();
		handler = new FallbackCacheErrorHandler(recorder);
		cache = mock(Cache.class);
		when(cache.getName()).thenReturn(CACHE_NAME);
	}

	@Nested
	@DisplayName("Spring 기본 처리기는")
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
	@DisplayName("캐시 조회가 실패하면")
	class WhenGetFails {

		@Test
		@DisplayName("예외를 삼켜 원본 조회로 넘어가게 한다")
		void swallowsSoOriginIsQueried() {
			assertThatNoException()
				 .as("조회 실패가 전파되면 캐시가 단일 장애점이 된다")
				 .isThrownBy(() -> handler.handleCacheGetError(REDIS_DOWN, cache, 1L));
		}

		@Test
		@DisplayName("실패 건수를 기록한다")
		void recordsFailure() {
			handler.handleCacheGetError(REDIS_DOWN, cache, 1L);

			assertThat(recorder.failureCount(CacheOperation.GET)).isOne();
		}
	}

	@Nested
	@DisplayName("캐시 적재가 실패하면")
	class WhenPutFails {

		@Test
		@DisplayName("예외를 삼켜 응답에 영향을 주지 않는다")
		void swallowsWithoutAffectingResponse() {
			assertThatNoException().isThrownBy(() -> handler.handleCachePutError(REDIS_DOWN, cache, 1L, "value"));
			assertThat(recorder.failureCount(CacheOperation.PUT)).isOne();
		}
	}

	@Nested
	@DisplayName("캐시 무효화가 실패하면")
	class WhenEvictFails {

		@Test
		@DisplayName("예외를 삼키되 실패를 반드시 기록한다")
		void swallowsButRecords() {
			assertThatNoException().isThrownBy(() -> handler.handleCacheEvictError(REDIS_DOWN, cache, 1L));

			assertThat(recorder.failureCount(CacheOperation.EVICT))
				 .as("무효화 유실은 stale을 남기므로 반드시 관측되어야 한다")
				 .isOne();
		}
	}

	@Nested
	@DisplayName("연산별 실패는")
	class FailureCounts {

		@Test
		@DisplayName("종류를 구분해 집계된다")
		void areCountedPerOperation() {
			handler.handleCacheGetError(REDIS_DOWN, cache, 1L);
			handler.handleCacheEvictError(REDIS_DOWN, cache, 1L);
			handler.handleCacheClearError(REDIS_DOWN, cache);

			assertThat(recorder.totalFailureCount()).isEqualTo(3);
			assertThat(recorder.failureCount(CacheOperation.PUT)).isZero();
		}
	}
}
