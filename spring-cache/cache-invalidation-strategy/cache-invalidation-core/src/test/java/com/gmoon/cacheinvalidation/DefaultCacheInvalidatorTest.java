package com.gmoon.cacheinvalidation;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import com.gmoon.cacheinvalidation.EntityChange.Type;

@DisplayName("엔티티 변경 무효화 실행")
class DefaultCacheInvalidatorTest {

	private static final String CACHED_VALUE = "cached";

	private ConcurrentMapCacheManager caches;
	private Cache userCache;
	private CacheManager cacheManager;

	@BeforeEach
	void setUp() {
		caches = new ConcurrentMapCacheManager(TestCachePolicy.Name.USER, TestCachePolicy.Name.USER_SUMMARY);
		userCache = spy(caches.getCache(TestCachePolicy.Name.USER));
		cacheManager = mock(CacheManager.class);
		given(cacheManager.getCache(anyString())).willAnswer(call -> {
			String name = call.getArgument(0);
			return TestCachePolicy.Name.USER.equals(name) ? userCache : caches.getCache(name);
		});
	}

	@Nested
	@DisplayName("캐시 키가 변경되는 수정이면")
	class WhenCacheKeyChanged {

		@Test
		@DisplayName("이전 상태로 재구성한 옛 키도 무효화한다")
		void evictsPreviousKeyAsWell() {
			putCached(TestCachePolicy.Name.USER, "before");
			putCached(TestCachePolicy.Name.USER, "after");

			invalidatorOf(previousUsernameRule(), currentUsernameRule())
				 .invalidate(EntityChange.of(new NaturalKeyUser("after"), Type.UPDATE,
					  EntityChange.State.of(new String[] {"username"}, new Object[] {"before"})));

			assertThat(cachedValue(TestCachePolicy.Name.USER, "before"))
				 .as("옛 키를 지우지 않으면 자연키 캐시가 영구히 stale 로 남는다")
				 .isNull();
			assertThat(cachedValue(TestCachePolicy.Name.USER, "after"))
				 .as("새 키도 함께 지워야 다음 조회가 DB 를 읽는다")
				 .isNull();
		}
	}

	@Nested
	@DisplayName("변경 하나에 여러 규칙이 반응하면")
	class WhenMultipleRulesMatch {

		@Test
		@DisplayName("모든 규칙의 대상을 합쳐 무효화한다")
		void mergesEntriesFromEveryMatchedRule() {
			putCached(TestCachePolicy.Name.USER, "1");
			putCached(TestCachePolicy.Name.USER_SUMMARY, "1");

			invalidatorOf(ruleOf(true, CacheKey.of(TestCachePolicy.USER, 1L)),
				 ruleOf(true, CacheKey.of(TestCachePolicy.USER_SUMMARY, 1L)))
				 .invalidate(changeOf(new Object()));

			assertThat(cachedValue(TestCachePolicy.Name.USER, "1"))
				 .as("첫 번째 규칙이 지목한 캐시")
				 .isNull();
			assertThat(cachedValue(TestCachePolicy.Name.USER_SUMMARY, "1"))
				 .as("교차 엔티티 규칙이 추가되어도 기존 규칙과 함께 동작해야 한다")
				 .isNull();
		}
	}

	@Nested
	@DisplayName("서로 다른 규칙이 같은 대상을 지목하면")
	class WhenRulesOverlap {

		@Test
		@DisplayName("중복을 제거해 한 번만 지운다")
		void deduplicatesEntries() {
			invalidatorOf(ruleOf(true, CacheKey.of(TestCachePolicy.USER, 1L)),
				 ruleOf(true, CacheKey.of(TestCachePolicy.USER, 1L)))
				 .invalidate(changeOf(new Object()));

			then(userCache).should(times(1).description("같은 키를 두 번 지우면 왕복만 늘고 결과는 같다"))
				 .evict("1");
		}
	}

	@Nested
	@DisplayName("반응하는 규칙이 없으면")
	class WhenNoRuleMatches {

		@Test
		@DisplayName("아무 캐시도 건드리지 않는다")
		void evictsNothing() {
			invalidatorOf(ruleOf(false, CacheKey.of(TestCachePolicy.USER, 1L)))
				 .invalidate(changeOf(new Object()));

			then(userCache).should(never()).evict(any());
		}
	}

	@Nested
	@DisplayName("규칙 하나가 예외를 던지면")
	class WhenRuleThrows {

		@Test
		@DisplayName("나머지 규칙은 계속 수행된다")
		void keepsResolvingRemainingRules() {
			putCached(TestCachePolicy.Name.USER, "1");

			invalidatorOf(failingRule(), ruleOf(true, CacheKey.of(TestCachePolicy.USER, 1L)))
				 .invalidate(changeOf(new Object()));

			assertThat(cachedValue(TestCachePolicy.Name.USER, "1"))
				 .as("규칙 하나의 실패가 다른 캐시의 무효화를 막으면 stale 이 남는다")
				 .isNull();
		}
	}

	@Nested
	@DisplayName("등록된 캐시를 지목하면")
	class WhenCacheRegistered {

		@Test
		@DisplayName("값이 없어도 삭제를 요청한다")
		void evictsEvenWhenAbsent() {
			invalidatorOf(ruleOf(true, CacheKey.of(TestCachePolicy.USER, 99L)))
				 .invalidate(changeOf(new Object()));

			then(userCache).should(description(
				 "존재 여부를 미리 확인하면 왕복이 늘고, 확인과 삭제 사이에 값이 다시 적재될 수 있다"))
				 .evict("99");
		}

		@Test
		@DisplayName("실제로 캐시에서 값을 제거한다")
		void removesCachedValue() {
			putCached(TestCachePolicy.Name.USER, "1");

			invalidatorOf(ruleOf(true, CacheKey.of(TestCachePolicy.USER, 1L)))
				 .invalidate(changeOf(new Object()));

			assertThat(cachedValue(TestCachePolicy.Name.USER, "1")).isNull();
		}
	}

	@Nested
	@DisplayName("등록되지 않은 캐시를 지목하면")
	class WhenCacheNotRegistered {

		@Test
		@DisplayName("예외를 던지지 않고 넘어간다")
		void skipsWithoutThrowing() {
			CacheInvalidator invalidator = new DefaultCacheInvalidator(
				 List.of(ruleOf(true, CacheKey.of(TestCachePolicy.USER_SUMMARY, 1L))),
				 new ConcurrentMapCacheManager(TestCachePolicy.Name.USER));

			assertThatNoException()
				 .as("설정 실수가 이미 커밋된 트랜잭션의 호출자를 깨뜨리면 안 된다")
				 .isThrownBy(() -> invalidator.invalidate(changeOf(new Object())));
		}
	}

	@Nested
	@DisplayName("캐시 인프라가 예외를 던지면")
	class WhenCacheThrows {

		@Test
		@DisplayName("캐시를 찾는 단계에서 터져도 전파하지 않는다")
		void swallowsWhenResolvingCacheThrows() {
			assertThatNoException()
				 .as("커밋 이후 실행되므로 예외를 던지면 이미 커밋된 트랜잭션의 호출자가 깨진다")
				 .isThrownBy(() -> invalidatorOn(FailingCacheManager.of("redis down"))
					  .invalidate(changeOf(new Object())));
		}

		@Test
		@DisplayName("삭제 단계에서 터져도 전파하지 않는다")
		void swallowsWhenEvictThrows() {
			assertThatNoException()
				 .as("방어가 캐시 조회만 감싸면 삭제 단계의 예외가 커밋 스레드로 새어나간다")
				 .isThrownBy(() -> invalidatorOn(new EvictThrowingCacheManager())
					  .invalidate(changeOf(new Object())));
		}
	}

	@Nested
	@DisplayName("규칙 산출 자체가 예외를 던지면")
	class WhenResolutionThrows {

		@Test
		@DisplayName("호출자에게 예외를 돌려주지 않는다")
		void doesNotPropagate() {
			CacheInvalidator invalidator = new DefaultCacheInvalidator(
				 List.of(throwingSupportRule()), cacheManager);

			assertThatNoException()
				 .as("신호는 트랜잭션 안에서 도착하므로 예외가 새면 캐시 때문에 호출자의 트랜잭션이 롤백된다")
				 .isThrownBy(() -> invalidator.invalidate(changeOf(new Object())));
		}
	}

	private CacheInvalidator invalidatorOf(InvalidationRule... rules) {
		return new DefaultCacheInvalidator(List.of(rules), cacheManager);
	}

	private CacheInvalidator invalidatorOn(CacheManager failing) {
		return new DefaultCacheInvalidator(
			 List.of(ruleOf(true, CacheKey.of(TestCachePolicy.USER, 1L))), failing);
	}

	private static EntityChange changeOf(Object entity) {
		return EntityChange.of(entity, Type.UPDATE);
	}

	private void putCached(String cacheName, String key) {
		caches.getCache(cacheName).put(key, CACHED_VALUE);
	}

	private Object cachedValue(String cacheName, String key) {
		return caches.getCache(cacheName).get(key, String.class);
	}

	private static InvalidationRule previousUsernameRule() {
		return rule(change -> change.previousValueOf("username")
			 .map(username -> List.of(CacheKey.of(TestCachePolicy.USER, username)))
			 .orElse(List.of()));
	}

	private static InvalidationRule currentUsernameRule() {
		return rule(change -> List.of(
			 CacheKey.of(TestCachePolicy.USER, ((NaturalKeyUser)change.entity()).username())));
	}

	private static InvalidationRule rule(Function<EntityChange, List<CacheKey>> resolver) {
		return new InvalidationRule() {
			@Override
			public boolean supports(EntityChange change) {
				return true;
			}

			@Override
			public Collection<CacheKey> resolve(EntityChange change) {
				return resolver.apply(change);
			}
		};
	}

	private static InvalidationRule ruleOf(boolean supported, CacheKey entry) {
		return new InvalidationRule() {
			@Override
			public boolean supports(EntityChange change) {
				return supported;
			}

			@Override
			public Collection<CacheKey> resolve(EntityChange change) {
				return List.of(entry);
			}
		};
	}

	private static InvalidationRule throwingSupportRule() {
		return new InvalidationRule() {
			@Override
			public boolean supports(EntityChange change) {
				throw new IllegalStateException("rule blew up outside resolve");
			}

			@Override
			public Collection<CacheKey> resolve(EntityChange change) {
				return List.of();
			}
		};
	}

	private static InvalidationRule failingRule() {
		return rule(change -> {
			throw new IllegalStateException("rule failed");
		});
	}

	private record NaturalKeyUser(String username) {
	}

	static class EvictThrowingCacheManager extends ConcurrentMapCacheManager {

		EvictThrowingCacheManager() {
			super(TestCachePolicy.Name.USER);
		}

		@Override
		public Cache getCache(String name) {
			return new EvictThrowingCache(super.getCache(name));
		}
	}

	record EvictThrowingCache(Cache delegate) implements Cache {

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
		public <T> T get(Object key, Callable<T> valueLoader) {
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
