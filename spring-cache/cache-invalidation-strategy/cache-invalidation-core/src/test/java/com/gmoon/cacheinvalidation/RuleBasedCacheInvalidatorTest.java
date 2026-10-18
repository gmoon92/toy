package com.gmoon.cacheinvalidation;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Collection;
import java.util.List;
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
class RuleBasedCacheInvalidatorTest {

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

			invalidatorOf(ruleOf(true, CacheEntry.of(TestCachePolicy.USER, 1L)),
				 ruleOf(true, CacheEntry.of(TestCachePolicy.USER_SUMMARY, 1L)))
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
			invalidatorOf(ruleOf(true, CacheEntry.of(TestCachePolicy.USER, 1L)),
				 ruleOf(true, CacheEntry.of(TestCachePolicy.USER, 1L)))
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
			invalidatorOf(ruleOf(false, CacheEntry.of(TestCachePolicy.USER, 1L)))
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

			invalidatorOf(failingRule(), ruleOf(true, CacheEntry.of(TestCachePolicy.USER, 1L)))
				 .invalidate(changeOf(new Object()));

			assertThat(cachedValue(TestCachePolicy.Name.USER, "1"))
				 .as("규칙 하나의 실패가 다른 캐시의 무효화를 막으면 stale 이 남는다")
				 .isNull();
		}
	}

	@Nested
	@DisplayName("규칙 산출 자체가 예외를 던지면")
	class WhenResolutionThrows {

		@Test
		@DisplayName("호출자에게 예외를 돌려주지 않는다")
		void doesNotPropagate() {
			CacheInvalidator invalidator = invalidatorOf(throwingSupportRule());

			assertThatNoException()
				 .as("신호는 트랜잭션 안에서 도착하므로 예외가 새면 캐시 때문에 호출자의 트랜잭션이 롤백된다")
				 .isThrownBy(() -> invalidator.invalidate(changeOf(new Object())));
		}
	}

	private CacheInvalidator invalidatorOf(InvalidationRule... rules) {
		AfterCommitEvictor evictor = new AfterCommitEvictor(
			 cacheManager,
			 Runnable::run
		);
		return new RuleBasedCacheInvalidator(
			 List.of(rules),
			 evictor
		);
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
			 .map(username -> List.of(CacheEntry.of(TestCachePolicy.USER, username)))
			 .orElse(List.of()));
	}

	private static InvalidationRule currentUsernameRule() {
		return rule(change -> List.of(
			 CacheEntry.of(TestCachePolicy.USER, ((NaturalKeyUser)change.entity()).username())));
	}

	private static InvalidationRule rule(Function<EntityChange, List<CacheEntry>> resolver) {
		return new InvalidationRule() {
			@Override
			public boolean supports(EntityChange change) {
				return true;
			}

			@Override
			public Collection<CacheEntry> resolve(EntityChange change) {
				return resolver.apply(change);
			}
		};
	}

	private static InvalidationRule ruleOf(boolean supported, CacheEntry entry) {
		return new InvalidationRule() {
			@Override
			public boolean supports(EntityChange change) {
				return supported;
			}

			@Override
			public Collection<CacheEntry> resolve(EntityChange change) {
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
			public Collection<CacheEntry> resolve(EntityChange change) {
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
}
