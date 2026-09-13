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

import com.gmoon.cacheinvalidation.core.fixture.FailingCacheManager;
import com.gmoon.cacheinvalidation.core.fixture.TestCachePolicy;
import com.gmoon.cacheinvalidation.core.invalidation.change.ChangeSource;
import com.gmoon.cacheinvalidation.core.invalidation.change.EntityChange;
import com.gmoon.cacheinvalidation.core.invalidation.change.EntityState;

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

			invalidator.invalidate(EntityChange.updated(new CacheableUser(1L), null), SOURCE);

			assertThat(cachedValue(TestCachePolicy.Name.USER, "1"))
				 .as("엔티티가 선언한 첫 번째 캐시")
				 .isNull();
			assertThat(cachedValue(TestCachePolicy.Name.USER_SUMMARY, "1"))
				 .as("엔티티가 선언한 두 번째 캐시도 함께 지워져야 한다")
				 .isNull();
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

			invalidator.invalidate(EntityChange.updated(new NaturalKeyUser("after"),
				 EntityState.of(new String[] {"username"}, new Object[] {"before"})), SOURCE);

			assertThat(cachedValue(TestCachePolicy.Name.USER, "before"))
				 .as("옛 키를 지우지 않으면 자연키 캐시가 영구히 stale로 남는다")
				 .isNull();
			assertThat(cachedValue(TestCachePolicy.Name.USER, "after"))
				 .as("새 키도 함께 지워야 다음 조회가 DB 를 읽는다")
				 .isNull();
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
				 .isThrownBy(() -> invalidator.invalidate(EntityChange.deleted(new CacheableUser(1L)), SOURCE));
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
			 .map(username -> List.of(CacheKey.of(TestCachePolicy.USER, username)))
			 .orElse(List.of()));
	}

	private InvalidationRule currentUsernameRule() {
		return rule(change -> List.of(
			 CacheKey.of(TestCachePolicy.USER, ((NaturalKeyUser)change.entity()).username())));
	}

	private InvalidationRule rule(Function<EntityChange, List<CacheKey>> resolver) {
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

	private record NaturalKeyUser(String username) {
	}

	private record CacheableUser(Long id) implements CacheEvictable {
		@Override
		public List<CacheKey> cacheEntriesToEvict() {
			return List.of(
				 CacheKey.of(TestCachePolicy.USER, id),
				 CacheKey.of(TestCachePolicy.USER_SUMMARY, id));
		}
	}
}
