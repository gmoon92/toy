package com.gmoon.cacheinvalidation;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationUtils;

import com.gmoon.cacheinvalidation.EntityChange.Source;
import com.gmoon.cacheinvalidation.EntityChange.Type;
import com.gmoon.cacheinvalidation.InvalidationRecorder.Outcome;

@DisplayName("엔티티 변경 무효화 실행")
class DefaultCacheInvalidatorTest {

	private static final String CACHED_VALUE = "cached";
	private static final Source SOURCE = Source.JPA_ENTITY;

	private CacheManager cacheManager;
	private InvalidationRecorder recorder;

	@BeforeEach
	void setUp() {
		cacheManager = new ConcurrentMapCacheManager(TestCachePolicy.Name.USER, TestCachePolicy.Name.USER_SUMMARY);
		recorder = mock(InvalidationRecorder.class);
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
				 .invalidate(EntityChange.committed(new NaturalKeyUser("after"), Type.UPDATE,
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
		@DisplayName("중복을 제거해 한 번만 무효화한다")
		void deduplicatesEntries() {
			invalidatorOf(ruleOf(true, CacheKey.of(TestCachePolicy.USER, 1L)),
				 ruleOf(true, CacheKey.of(TestCachePolicy.USER, 1L)))
				 .invalidate(changeOf(new Object()));

			verify(recorder, times(1).description("같은 키를 두 번 지우면 무효화 지표가 부풀려진다"))
				 .recordEviction(TestCachePolicy.Name.USER, SOURCE, Outcome.EVICT_REQUESTED);
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

			verifyNoInteractions(recorder);
		}
	}

	@Nested
	@DisplayName("규칙 하나가 예외를 던지면")
	class WhenRuleThrows {

		@Test
		@DisplayName("나머지 규칙은 계속 수행된다")
		void keepsResolvingRemainingRules() {
			invalidatorOf(failingRule(), ruleOf(true, CacheKey.of(TestCachePolicy.USER, 1L)))
				 .invalidate(changeOf(new Object()));

			verify(recorder, description("규칙 하나의 실패가 다른 캐시의 무효화를 막으면 stale 이 남는다"))
				 .recordEviction(TestCachePolicy.Name.USER, SOURCE, Outcome.EVICT_REQUESTED);
			verify(recorder).recordRuleFailure(anyString());
		}
	}

	@Nested
	@DisplayName("등록된 캐시를 지목하면")
	class WhenCacheRegistered {

		@Test
		@DisplayName("값이 있든 없든 EVICT_REQUESTED 로 기록한다")
		void recordsEvictRequestedRegardlessOfPresence() {
			putCached(TestCachePolicy.Name.USER, "1");

			invalidatorOf(ruleOf(true, CacheKey.of(TestCachePolicy.USER, 1L)))
				 .invalidate(changeOf(new Object()));
			invalidatorOf(ruleOf(true, CacheKey.of(TestCachePolicy.USER, 99L)))
				 .invalidate(changeOf(new Object()));

			verify(recorder, times(2).description(
				 "Cache.evictIfPresent 의 기본 구현은 항상 false 를 반환하고 RedisCache 는 이를 재정의하지 않는다."
					  + " 존재 여부를 결과로 노출하면 캐시 구현에 따라 의미가 달라진다"))
				 .recordEviction(eq(TestCachePolicy.Name.USER), eq(SOURCE), eq(Outcome.EVICT_REQUESTED));
		}

		@Test
		@DisplayName("실제로 캐시에서 값을 제거한다")
		void removesCachedValue() {
			putCached(TestCachePolicy.Name.USER, "1");

			invalidatorOf(ruleOf(true, CacheKey.of(TestCachePolicy.USER, 1L)))
				 .invalidate(changeOf(new Object()));

			assertThat(cachedValue(TestCachePolicy.Name.USER, "1"))
				 .as("결과 코드와 무관하게 삭제 자체는 반드시 수행되어야 한다")
				 .isNull();
		}
	}

	@Nested
	@DisplayName("트랜잭션 안에서 여러 신호가 같은 키를 지목하면")
	class WhenSeveralSignalsTargetSameKeyInTransaction {

		@Test
		@DisplayName("커밋 이후 한 번만 지운다")
		void evictsOnceAfterCommit() {
			CacheInvalidator invalidator = invalidatorOf(ruleOf(true, CacheKey.of(TestCachePolicy.USER, 1L)));

			inTransaction(TransactionSynchronization.STATUS_COMMITTED, () -> {
				invalidator.invalidate(EntityChange.committed(new Object(), Type.UPDATE, null));
				invalidator.invalidate(EntityChange.published(new Object(), Type.UPDATE));

				verify(recorder, never().description("모으는 동안에는 캐시를 건드리지 않는다"))
					 .recordEviction(any(), any(), any());
			});

			verify(recorder, times(1).description(
				 "신호원마다 즉시 지우면 같은 키를 두 번 지운다. 모았다가 커밋 이후 한 번에 처리해야 한다"))
				 .recordEviction(eq(TestCachePolicy.Name.USER), any(), eq(Outcome.EVICT_REQUESTED));
		}

		@Test
		@DisplayName("롤백되면 모인 대상을 버린다")
		void discardsOnRollback() {
			CacheInvalidator invalidator = invalidatorOf(ruleOf(true, CacheKey.of(TestCachePolicy.USER, 1L)));

			inTransaction(TransactionSynchronization.STATUS_ROLLED_BACK, () ->
				 invalidator.invalidate(EntityChange.committed(new Object(), Type.UPDATE, null)));

			verify(recorder, never().description("커밋되지 않은 변경으로 캐시를 지우면 멀쩡한 값을 버린다"))
				 .recordEviction(any(), any(), any());
		}
	}

	@Nested
	@DisplayName("등록되지 않은 캐시를 지목하면")
	class WhenCacheNotRegistered {

		@Test
		@DisplayName("예외 대신 CACHE_NOT_REGISTERED 로 기록한다")
		void recordsCacheNotRegistered() {
			CacheInvalidator invalidator = new DefaultCacheInvalidator(
				 List.of(ruleOf(true, CacheKey.of(TestCachePolicy.USER_SUMMARY, 1L))),
				 new ConcurrentMapCacheManager(TestCachePolicy.Name.USER),
				 recorder);

			invalidator.invalidate(changeOf(new Object()));

			verify(recorder).recordEviction(
				 TestCachePolicy.Name.USER_SUMMARY, SOURCE, Outcome.CACHE_NOT_REGISTERED);
		}
	}

	@Nested
	@DisplayName("캐시 인프라가 예외를 던지면")
	class WhenCacheThrows {

		@Test
		@DisplayName("캐시를 찾는 단계에서 터져도 FAILED 로 기록한다")
		void recordsFailedWhenResolvingCacheThrows() {
			invalidatorOn(FailingCacheManager.of("redis down")).invalidate(changeOf(new Object()));

			verify(recorder, description("커밋 이후 실행되므로 예외를 던지면 이미 커밋된 트랜잭션의 호출자가 깨진다"))
				 .recordEviction(TestCachePolicy.Name.USER, SOURCE, Outcome.FAILED);
		}

		@Test
		@DisplayName("삭제 단계에서 터져도 FAILED 로 기록한다")
		void recordsFailedWhenEvictThrows() {
			invalidatorOn(new EvictThrowingCacheManager()).invalidate(changeOf(new Object()));

			verify(recorder, description("방어가 캐시 조회만 감싸면 삭제 단계의 예외가 커밋 스레드로 새어나간다"))
				 .recordEviction(TestCachePolicy.Name.USER, SOURCE, Outcome.FAILED);
		}
	}

	private static void inTransaction(int completionStatus, Runnable work) {
		TransactionSynchronizationManager.initSynchronization();
		try {
			work.run();
			TransactionSynchronizationUtils.triggerAfterCompletion(completionStatus);
		} finally {
			TransactionSynchronizationManager.clearSynchronization();
		}
	}

	private CacheInvalidator invalidatorOf(InvalidationRule... rules) {
		return new DefaultCacheInvalidator(List.of(rules), cacheManager, recorder);
	}

	private CacheInvalidator invalidatorOn(CacheManager failing) {
		return new DefaultCacheInvalidator(
			 List.of(ruleOf(true, CacheKey.of(TestCachePolicy.USER, 1L))), failing, recorder);
	}

	private static EntityChange changeOf(Object entity) {
		return EntityChange.committed(entity, Type.UPDATE, null);
	}

	private void putCached(String cacheName, String key) {
		cacheManager.getCache(cacheName).put(key, CACHED_VALUE);
	}

	private Object cachedValue(String cacheName, String key) {
		return cacheManager.getCache(cacheName).get(key, String.class);
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
