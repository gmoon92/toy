package com.gmoon.cacheinvalidation.core.invalidation;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import com.gmoon.cacheinvalidation.core.fixture.FailingCacheManager;
import com.gmoon.cacheinvalidation.core.fixture.TestCachePolicy;

@DisplayName("캐시 무효화 실행 결과")
class CacheEvictorTest {

	private CacheManager cacheManager;
	private CacheEvictor cacheEvictor;

	@BeforeEach
	void setUp() {
		cacheManager = new ConcurrentMapCacheManager(TestCachePolicy.Name.USER);
		cacheEvictor = new CacheEvictor(cacheManager);
	}

	@Nested
	@DisplayName("등록된 캐시를 지목하면")
	class WhenCacheRegistered {

		@Test
		@DisplayName("캐시에 값이 있든 없든 EVICT_REQUESTED 를 반환한다")
		void returnsEvictRequestedRegardlessOfPresence() {
			cacheManager.getCache(TestCachePolicy.Name.USER).put("1", "cached");

			assertThat(cacheEvictor.evict(CacheKey.of(TestCachePolicy.USER, 1L)))
				 .as("값이 있을 때의 결과")
				 .isEqualTo(EvictionOutcome.EVICT_REQUESTED);
			assertThat(cacheEvictor.evict(CacheKey.of(TestCachePolicy.USER, 2L)))
				 .as("Cache.evictIfPresent 의 기본 구현은 항상 false 를 반환하고 RedisCache 는 이를 재정의하지 않는다."
					  + " 존재 여부를 결과로 노출하면 캐시 구현에 따라 의미가 달라진다")
				 .isEqualTo(EvictionOutcome.EVICT_REQUESTED);
		}

		@Test
		@DisplayName("실제로 캐시에서 값을 제거한다")
		void removesCachedValue() {
			cacheManager.getCache(TestCachePolicy.Name.USER).put("1", "cached");

			cacheEvictor.evict(CacheKey.of(TestCachePolicy.USER, 1L));

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
			assertThat(cacheEvictor.evict(CacheKey.of(TestCachePolicy.USER_SUMMARY, 1L)))
				 .isEqualTo(EvictionOutcome.CACHE_NOT_REGISTERED);
		}
	}

	@Nested
	@DisplayName("캐시 인프라가 예외를 던지면")
	class WhenCacheThrows {

		@Test
		@DisplayName("캐시를 찾는 단계에서 터져도 FAILED 를 반환한다")
		void returnsFailedWhenResolvingCacheThrows() {
			CacheEvictor evictor = new CacheEvictor(FailingCacheManager.of("redis down"));

			assertThat(evictor.evict(CacheKey.of(TestCachePolicy.USER, 1L)))
				 .as("커밋 이후 실행되므로 예외를 던지면 이미 커밋된 트랜잭션의 호출자가 깨진다")
				 .isEqualTo(EvictionOutcome.FAILED);
		}

		@Test
		@DisplayName("삭제 단계에서 터져도 FAILED 를 반환한다")
		void returnsFailedWhenEvictThrows() {
			CacheEvictor evictor = new CacheEvictor(new EvictThrowingCacheManager());

			assertThat(evictor.evict(CacheKey.of(TestCachePolicy.USER, 1L)))
				 .as("방어가 캐시 조회만 감싸면 삭제 단계의 예외가 커밋 스레드로 새어나간다")
				 .isEqualTo(EvictionOutcome.FAILED);
		}
	}

	static class EvictThrowingCacheManager extends ConcurrentMapCacheManager {

		EvictThrowingCacheManager() {
			super(TestCachePolicy.Name.USER);
		}

		@Override
		public Cache getCache(String name) {
			Cache delegate = super.getCache(name);
			return new ConcurrentMapCacheWrapper(delegate);
		}
	}

	static class ConcurrentMapCacheWrapper implements Cache {

		private final Cache delegate;

		ConcurrentMapCacheWrapper(Cache delegate) {
			this.delegate = delegate;
		}

		@Override
		public String getName() {
			return delegate.getName();
		}

		@Override
		public Object getNativeCache() {
			return delegate.getNativeCache();
		}

		@Override
		public ValueWrapper get(Object key) {
			return delegate.get(key);
		}

		@Override
		public <T> T get(Object key, Class<T> type) {
			return delegate.get(key, type);
		}

		@Override
		public <T> T get(Object key, java.util.concurrent.Callable<T> valueLoader) {
			return delegate.get(key, valueLoader);
		}

		@Override
		public void put(Object key, Object value) {
			delegate.put(key, value);
		}

		@Override
		public void evict(Object key) {
			throw new IllegalStateException("redis down");
		}

		@Override
		public void clear() {
			delegate.clear();
		}
	}
}
