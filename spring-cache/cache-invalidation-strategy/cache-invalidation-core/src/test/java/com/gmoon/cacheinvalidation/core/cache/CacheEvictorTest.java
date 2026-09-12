package com.gmoon.cacheinvalidation.core.cache;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import com.gmoon.cacheinvalidation.core.fixture.TestCachePolicy;
import com.gmoon.cacheinvalidation.core.resilience.CacheFailureRecorder;
import com.gmoon.cacheinvalidation.core.resilience.CacheOperation;

@DisplayName("캐시 무효화 실행 결과")
class CacheEvictorTest {

	private CacheManager cacheManager;
	private CacheFailureRecorder failureRecorder;
	private CacheEvictor cacheEvictor;

	@BeforeEach
	void setUp() {
		cacheManager = new ConcurrentMapCacheManager(TestCachePolicy.Name.USER);
		failureRecorder = new CacheFailureRecorder();
		cacheEvictor = new CacheEvictor(cacheManager, failureRecorder);
	}

	@Nested
	@DisplayName("등록된 캐시를 지목하면")
	class WhenCacheRegistered {

		@Test
		@DisplayName("캐시에 값이 있든 없든 EVICT_REQUESTED 를 반환한다")
		void returnsEvictRequestedRegardlessOfPresence() {
			cacheManager.getCache(TestCachePolicy.Name.USER).put("1", "cached");

			assertThat(cacheEvictor.evict(CacheEntryRef.of(TestCachePolicy.USER, 1L)))
				 .isEqualTo(EvictionOutcome.EVICT_REQUESTED);
			assertThat(cacheEvictor.evict(CacheEntryRef.of(TestCachePolicy.USER, 2L)))
				 .as("Cache.evictIfPresent 의 기본 구현은 항상 false 를 반환하고 RedisCache 는 이를 재정의하지 않는다."
					  + " 존재 여부를 결과로 노출하면 캐시 구현에 따라 의미가 달라진다")
				 .isEqualTo(EvictionOutcome.EVICT_REQUESTED);
		}

		@Test
		@DisplayName("실제로 캐시에서 값을 제거한다")
		void removesCachedValue() {
			cacheManager.getCache(TestCachePolicy.Name.USER).put("1", "cached");

			cacheEvictor.evict(CacheEntryRef.of(TestCachePolicy.USER, 1L));

			assertThat(cacheManager.getCache(TestCachePolicy.Name.USER).get("1"))
				 .as("결과 코드와 무관하게 삭제 자체는 반드시 수행되어야 한다")
				 .isNull();
		}
	}

	@Nested
	@DisplayName("등록되지 않은 캐시를 지목하면")
	class WhenCacheNotRegistered {

		@Test
		@DisplayName("예외 대신 CACHE_NOT_REGISTERED 를 반환한다")
		void returnsCacheNotRegistered() {
			assertThat(cacheEvictor.evict(CacheEntryRef.of(TestCachePolicy.USER_SUMMARY, 1L)))
				 .isEqualTo(EvictionOutcome.CACHE_NOT_REGISTERED);
		}
	}

	@Nested
	@DisplayName("캐시 인프라가 예외를 던지면")
	class WhenCacheThrows {

		@Test
		@DisplayName("FAILED 를 반환하고 예외를 전파하지 않는다")
		void returnsFailedWithoutPropagating() {
			CacheEvictor evictor = new CacheEvictor(throwingCacheManager(), failureRecorder);

			assertThat(evictor.evict(CacheEntryRef.of(TestCachePolicy.USER, 1L)))
				 .as("커밋 이후 실행되므로 예외를 던지면 이미 커밋된 트랜잭션의 호출자가 깨진다")
				 .isEqualTo(EvictionOutcome.FAILED);
		}

		@Test
		@DisplayName("캐시 조회 단계에서 터져도 실패로 기록한다")
		void recordsFailureRaisedWhileResolvingCache() {
			CacheEvictor evictor = new CacheEvictor(throwingCacheManager(), failureRecorder);

			evictor.evict(CacheEntryRef.of(TestCachePolicy.USER, 1L));

			assertThat(failureRecorder.failureCount(CacheOperation.EVICT))
				 .as("getCache 단계의 예외가 방어 밖에 있으면 커밋 스레드로 새어나간다")
				 .isEqualTo(1);
		}
	}

	private CacheManager throwingCacheManager() {
		return new CacheManager() {
			@Override
			public Cache getCache(String name) {
				throw new IllegalStateException("redis down");
			}

			@Override
			public java.util.Collection<String> getCacheNames() {
				return java.util.List.of();
			}
		};
	}
}
