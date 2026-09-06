package com.gmoon.cacheinvalidation.core.invalidation;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import com.gmoon.cacheinvalidation.core.cache.CacheEntryRef;
import com.gmoon.cacheinvalidation.core.cache.CacheEvictable;
import com.gmoon.cacheinvalidation.core.cache.CacheEvictor;
import com.gmoon.cacheinvalidation.core.fixture.TestCachePolicy;
import com.gmoon.cacheinvalidation.core.resilience.CacheFailureRecorder;
import com.gmoon.cacheinvalidation.core.resilience.CacheOperation;

@DisplayName("엔티티 변경 무효화 실행")
class EntityChangeInvalidatorTest {

	private static final String CACHED_VALUE = "cached";

	private CacheManager cacheManager;
	private CacheFailureRecorder failureRecorder;

	@BeforeEach
	void setUp() {
		cacheManager = new ConcurrentMapCacheManager(TestCachePolicy.Name.USER, TestCachePolicy.Name.USER_SUMMARY);
		failureRecorder = new CacheFailureRecorder();
	}

	@Nested
	@DisplayName("엔티티가 CacheEvictable을 구현하면")
	class WhenEntityDeclaresItsOwnEntries {

		@Test
		@DisplayName("내장 규칙이 선언된 캐시를 모두 무효화한다")
		void evictsEveryDeclaredEntry() {
			putCached(TestCachePolicy.Name.USER, "1");
			putCached(TestCachePolicy.Name.USER_SUMMARY, "1");
			EntityChangeInvalidator invalidator = invalidatorOf(new CacheEvictableRule());

			invalidator.invalidate(EntityChange.updated(new CacheableUser(1L), 1L, null, null));

			assertThat(cachedValue(TestCachePolicy.Name.USER, "1")).isNull();
			assertThat(cachedValue(TestCachePolicy.Name.USER_SUMMARY, "1")).isNull();
		}
	}

	@Nested
	@DisplayName("캐시 키가 변경되는 수정이면")
	class WhenCacheKeyChanged {

		@Test
		@DisplayName("이전 상태로 재구성한 옛 키도 무효화한다")
		void evictsPreviousKeyAsWell() {
			putCached(TestCachePolicy.Name.USER, "before");
			putCached(TestCachePolicy.Name.USER, "after");
			EntityChangeInvalidator invalidator = invalidatorOf(previousUsernameRule(), currentUsernameRule());

			invalidator.invalidate(EntityChange.updated(
				 new NaturalKeyUser("after"), 1L, new Object[] {"before"}, new String[] {"username"}));

			assertThat(cachedValue(TestCachePolicy.Name.USER, "before"))
				 .as("옛 키를 지우지 않으면 자연키 캐시가 영구히 stale로 남는다")
				 .isNull();
			assertThat(cachedValue(TestCachePolicy.Name.USER, "after")).isNull();
		}
	}

	@Nested
	@DisplayName("무효화가 실패해도")
	class WhenEvictionFails {

		@Test
		@DisplayName("예외를 전파하지 않고 실패를 기록한다")
		void recordsFailureWithoutPropagating() {
			CacheManager failing = new ConcurrentMapCacheManager(TestCachePolicy.Name.USER) {
				@Override
				public org.springframework.cache.Cache getCache(String name) {
					org.springframework.cache.Cache delegate = super.getCache(name);
					return new org.springframework.cache.support.AbstractValueAdaptingCache(false) {
						@Override public String getName() { return delegate.getName(); }
						@Override public Object getNativeCache() { return delegate.getNativeCache(); }
						@Override protected Object lookup(Object key) { return null; }
						@Override public <T> T get(Object key, java.util.concurrent.Callable<T> valueLoader) { return null; }
						@Override public void put(Object key, Object value) { }
						@Override public void evict(Object key) { throw new IllegalStateException("redis down"); }
						@Override public boolean evictIfPresent(Object key) { throw new IllegalStateException("redis down"); }
						@Override public void clear() { }
					};
				}
			};
			EntityChangeInvalidator invalidator = new EntityChangeInvalidator(
				 new CacheInvalidationRules(List.of(new CacheEvictableRule())),
				 new CacheEvictor(failing, failureRecorder));

			assertThatNoException()
				 .as("무효화 실패가 전파되면 커밋된 트랜잭션 이후 흐름이 깨진다")
				 .isThrownBy(() -> invalidator.invalidate(EntityChange.deleted(new CacheableUser(1L), 1L)));
			assertThat(failureRecorder.failureCount(CacheOperation.EVICT)).isEqualTo(2);
		}
	}

	private EntityChangeInvalidator invalidatorOf(CacheInvalidationRule... rules) {
		return new EntityChangeInvalidator(
			 new CacheInvalidationRules(List.of(rules)),
			 new CacheEvictor(cacheManager, failureRecorder));
	}

	private void putCached(String cacheName, String key) {
		cacheManager.getCache(cacheName).put(key, CACHED_VALUE);
	}

	private Object cachedValue(String cacheName, String key) {
		return cacheManager.getCache(cacheName).get(key, String.class);
	}

	private CacheInvalidationRule previousUsernameRule() {
		return rule(change -> change.previousValueOf("username")
			 .map(username -> List.of(CacheEntryRef.of(TestCachePolicy.USER, username)))
			 .orElse(List.of()));
	}

	private CacheInvalidationRule currentUsernameRule() {
		return rule(change -> List.of(
			 CacheEntryRef.of(TestCachePolicy.USER, ((NaturalKeyUser)change.entity()).username())));
	}

	private CacheInvalidationRule rule(java.util.function.Function<EntityChange, List<CacheEntryRef>> resolver) {
		return new CacheInvalidationRule() {
			@Override
			public boolean supports(EntityChange change) {
				return true;
			}

			@Override
			public java.util.Collection<CacheEntryRef> resolve(EntityChange change) {
				return resolver.apply(change);
			}
		};
	}

	private record NaturalKeyUser(String username) {
	}

	private record CacheableUser(Long id) implements CacheEvictable {
		@Override
		public List<CacheEntryRef> cacheEntriesToEvict() {
			return List.of(
				 CacheEntryRef.of(TestCachePolicy.USER, id),
				 CacheEntryRef.of(TestCachePolicy.USER_SUMMARY, id));
		}
	}
}
