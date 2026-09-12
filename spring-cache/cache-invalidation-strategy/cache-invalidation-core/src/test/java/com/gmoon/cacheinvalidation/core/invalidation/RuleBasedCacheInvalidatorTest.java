package com.gmoon.cacheinvalidation.core.invalidation;

import static org.assertj.core.api.Assertions.*;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import com.gmoon.cacheinvalidation.core.cache.eviction.CacheEntryRef;
import com.gmoon.cacheinvalidation.core.cache.eviction.CacheEvictable;
import com.gmoon.cacheinvalidation.core.cache.eviction.CacheEvictor;
import com.gmoon.cacheinvalidation.core.cache.eviction.EvictionOutcome;
import com.gmoon.cacheinvalidation.core.fixture.FailingCacheManager;
import com.gmoon.cacheinvalidation.core.fixture.TestCachePolicy;
import com.gmoon.cacheinvalidation.core.invalidation.metrics.InvalidationRecorder;
import com.gmoon.cacheinvalidation.core.invalidation.event.ChangeSource;
import com.gmoon.cacheinvalidation.core.invalidation.event.EntityChange;

@DisplayName("엔티티 변경 무효화 실행")
class RuleBasedCacheInvalidatorTest {

	private static final String CACHED_VALUE = "cached";
	private static final ChangeSource SOURCE = ChangeSource.JPA_ENTITY;

	private CacheManager cacheManager;
	private InvalidationRecorder invalidationRecorder;

	@BeforeEach
	void setUp() {
		cacheManager = new ConcurrentMapCacheManager(TestCachePolicy.Name.USER, TestCachePolicy.Name.USER_SUMMARY);
		invalidationRecorder = new InvalidationRecorder();
	}

	@Nested
	@DisplayName("엔티티가 CacheEvictable을 구현하면")
	class WhenEntityDeclaresItsOwnEntries {

		@Test
		@DisplayName("내장 규칙이 선언된 캐시를 모두 무효화한다")
		void evictsEveryDeclaredEntry() {
			putCached(TestCachePolicy.Name.USER, "1");
			putCached(TestCachePolicy.Name.USER_SUMMARY, "1");
			RuleBasedCacheInvalidator invalidator = invalidatorOf(EvictableEntityRule.owning(TestCachePolicy.USER, TestCachePolicy.USER_SUMMARY));

			invalidator.invalidate(EntityChange.updated(new CacheableUser(1L), 1L, null, null), SOURCE);

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
			RuleBasedCacheInvalidator invalidator = invalidatorOf(previousUsernameRule(), currentUsernameRule());

			invalidator.invalidate(EntityChange.updated(
				 new NaturalKeyUser("after"), 1L, new Object[] {"before"}, new String[] {"username"}), SOURCE);

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
			RuleBasedCacheInvalidator invalidator = new RuleBasedCacheInvalidator(
				 new InvalidationRuleSet(List.of(EvictableEntityRule.owning(TestCachePolicy.USER)),
					  invalidationRecorder),
				 new CacheEvictor(FailingCacheManager.of("redis down")),
				 invalidationRecorder);

			assertThatNoException()
				 .as("무효화 실패가 전파되면 커밋된 트랜잭션 이후 흐름이 깨진다")
				 .isThrownBy(() -> invalidator.invalidate(EntityChange.deleted(new CacheableUser(1L), 1L), SOURCE));
			assertThat(invalidationRecorder.evictionCount(
				 TestCachePolicy.Name.USER, SOURCE, EvictionOutcome.FAILED))
				 .as("실패가 결과로 기록되지 않으면 무효화 유실이 조용히 묻힌다")
				 .isEqualTo(1);
		}
	}

	private RuleBasedCacheInvalidator invalidatorOf(InvalidationRule... rules) {
		return new RuleBasedCacheInvalidator(
			 new InvalidationRuleSet(List.of(rules), invalidationRecorder),
			 new CacheEvictor(cacheManager),
			 invalidationRecorder);
	}

	private void putCached(String cacheName, String key) {
		cacheManager.getCache(cacheName).put(key, CACHED_VALUE);
	}

	private Object cachedValue(String cacheName, String key) {
		return cacheManager.getCache(cacheName).get(key, String.class);
	}

	private InvalidationRule previousUsernameRule() {
		return rule(change -> change.previousValueOf("username")
			 .map(username -> List.of(CacheEntryRef.of(TestCachePolicy.USER, username)))
			 .orElse(List.of()));
	}

	private InvalidationRule currentUsernameRule() {
		return rule(change -> List.of(
			 CacheEntryRef.of(TestCachePolicy.USER, ((NaturalKeyUser)change.entity()).username())));
	}

	private InvalidationRule rule(Function<EntityChange, List<CacheEntryRef>> resolver) {
		return new InvalidationRule() {
			@Override
			public boolean supports(EntityChange change) {
				return true;
			}

			@Override
			public Collection<CacheEntryRef> resolve(EntityChange change) {
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
