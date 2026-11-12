package com.gmoon.writeinvalidate.cache;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import com.gmoon.writeinvalidate.config.CachePolicy;

@DisplayName("커밋 이후 캐시 삭제")
class AfterCommitEvictorTest {

	private static final InvalidationMetrics NO_METRICS = new InvalidationMetrics(null);

	private static final CacheKey USER_1 = CacheKey.of(CachePolicy.USER, 1L);
	private static final CacheKey USER_2 = CacheKey.of(CachePolicy.USER, 2L);
	private static final CacheKey ARTICLE_1 = CacheKey.of(CachePolicy.ARTICLE, 1L);

	private RecordingCacheManager caches;
	private AfterCommitEvictor evictor;
	private PlatformTransactionManager transactionManager;

	@BeforeEach
	void setUp() {
		caches = new RecordingCacheManager(CachePolicy.Name.USER);
		evictor = new AfterCommitEvictor(
			 caches,
			 NO_METRICS
		);
		transactionManager = new StubTransactionManager();
	}

	@Nested
	@DisplayName("트랜잭션이 없으면")
	class WhenNoTransaction {

		@Test
		@DisplayName("모으지 않고 즉시 지운다")
		void evictsImmediately() {
			evict(USER_1);

			assertThat(caches.evictedKeys())
				 .as("모을 자리가 없는데 미루면 영영 지워지지 않는다")
				 .containsExactly(USER_1.key());
		}
	}

	@Nested
	@DisplayName("한 트랜잭션에서 같은 키를 여러 번 지목하면")
	class WhenSameKeyCollectedTwice {

		@Test
		@DisplayName("커밋 전에는 지우지 않는다")
		void defersUntilCompletion() {
			inTransaction(() -> {
				evict(USER_1);
				evict(USER_1);

				assertThat(caches.evictedKeys())
					 .as("커밋 전에 지우면 롤백된 변경으로 멀쩡한 값을 버린다")
					 .isEmpty();
			});
		}

		@Test
		@DisplayName("커밋 이후 지목한 만큼 지운다")
		void evictsAsManyAsRequestedAfterCommit() {
			inTransaction(() -> {
				evict(USER_1);
				evict(USER_1);
			});

			assertThat(caches.evictedKeys())
				 .as("중복을 걸러내지 않는다. DEL 은 여러 번 해도 결과가 같고, 걸러내려면 트랜잭션마다 수집함을 따로 들고 있어야 한다")
				 .containsExactly(USER_1.key(), USER_1.key());
		}
	}

	@Nested
	@DisplayName("트랜잭션이 롤백되면")
	class WhenRolledBack {

		@Test
		@DisplayName("지우지 않는다")
		void discardsRequestedKeys() {
			inTransaction(status -> {
				evict(USER_1);
				status.setRollbackOnly();
			});

			assertThat(caches.evictedKeys())
				 .as("커밋되지 않은 변경으로 캐시를 지우면 멀쩡한 값을 버린다")
				 .isEmpty();
		}
	}

	@Nested
	@DisplayName("REQUIRES_NEW 안쪽에서 지목하면")
	class WhenCollectedInRequiresNew {

		@Test
		@DisplayName("안쪽이 커밋되는 시점에 안쪽 키를 지운다")
		void evictsInnerKeyOnInnerCommit() {
			inTransaction(() -> {
				evict(USER_1);
				inNewTransaction(() -> evict(USER_2));

				assertThat(caches.evictedKeys())
					 .as("안쪽은 이미 커밋됐다. 바깥이 끝나기를 기다리면 그만큼 stale 이 길어진다")
					 .containsExactly(USER_2.key());
			});
		}

		@Test
		@DisplayName("바깥이 롤백해도 안쪽에서 커밋된 키는 지워진다")
		void keepsInnerEvictionWhenOuterRollsBack() {
			inTransaction(status -> {
				evict(USER_1);
				inNewTransaction(() -> evict(USER_2));
				status.setRollbackOnly();
			});

			assertThat(caches.evictedKeys())
				 .as("안쪽 삭제가 바깥 트랜잭션에 붙으면 바깥 롤백이 이미 커밋된 변경의 무효화를 버린다")
				 .containsExactly(USER_2.key());
		}

		@Test
		@DisplayName("바깥이 커밋되면 바깥 키는 그때 지운다")
		void evictsOuterKeyOnOuterCommit() {
			inTransaction(() -> {
				evict(USER_1);
				inNewTransaction(() -> evict(USER_2));
			});

			assertThat(caches.evictedKeys())
				 .as("각자 자기 트랜잭션에 붙으므로 안쪽이 먼저, 바깥이 나중에 지워진다")
				 .containsExactly(USER_2.key(), USER_1.key());
		}
	}

	@Nested
	@DisplayName("지울 캐시가 등록되어 있으면")
	class WhenCacheRegistered {

		@Test
		@DisplayName("값이 없어도 삭제를 요청한다")
		void evictsEvenWhenAbsent() {
			evict(USER_1);

			assertThat(caches.evictedKeys())
				 .as("존재 여부를 미리 확인하면 왕복이 늘고, 확인과 삭제 사이에 값이 다시 적재될 수 있다")
				 .containsExactly(USER_1.key());
		}

		@Test
		@DisplayName("캐시에서 값을 제거한다")
		void removesCachedValue() {
			caches.getCache(CachePolicy.Name.USER).put(USER_1.key(), "cached");

			evict(USER_1);

			assertThat(caches.getCache(CachePolicy.Name.USER).get(USER_1.key()))
				 .as("삭제 요청이 실제 저장소까지 닿아야 다음 조회가 DB 를 읽는다")
				 .isNull();
		}
	}

	@Nested
	@DisplayName("지울 캐시가 등록되어 있지 않으면")
	class WhenCacheNotRegistered {

		@Test
		@DisplayName("예외 없이 나머지 키를 지운다")
		void skipsAndContinues() {
			evict(ARTICLE_1, USER_1);

			assertThat(caches.evictedKeys())
				 .as("설정 실수 하나가 다른 캐시의 무효화를 막으면 stale 이 남는다")
				 .containsExactly(USER_1.key());
		}
	}

	@Nested
	@DisplayName("캐시 인프라가 예외를 던지면")
	class WhenCacheThrows {

		@Test
		@DisplayName("캐시를 찾다 터져도 전파하지 않는다")
		void swallowsLookupFailure() {
			AfterCommitEvictor failing = new AfterCommitEvictor(
				 FailingCacheManager.of("redis down"),
				 NO_METRICS
			);

			assertThatNoException()
				 .as("커밋 이후 실행되므로 예외를 던지면 이미 커밋된 트랜잭션의 호출자가 깨진다")
				 .isThrownBy(() -> failing.evict(entriesOf(USER_1)));
		}

		@Test
		@DisplayName("한 키의 삭제가 터져도 나머지 키는 지운다")
		void continuesAfterEvictFailure() {
			CacheManager partlyFailing = mock(CacheManager.class);
			given(partlyFailing.getCache(CachePolicy.Name.ARTICLE))
				 .willReturn(new EvictThrowingCache(new ConcurrentMapCache(CachePolicy.Name.ARTICLE)));
			given(partlyFailing.getCache(CachePolicy.Name.USER))
				 .willReturn(caches.getCache(CachePolicy.Name.USER));
			AfterCommitEvictor evictorOverFailure = new AfterCommitEvictor(
				 partlyFailing,
				 NO_METRICS
			);

			evictorOverFailure.evict(entriesOf(ARTICLE_1, USER_1));

			assertThat(caches.evictedKeys())
				 .as("방어가 묶음 전체를 감싸면 첫 실패 뒤의 키가 모두 stale 로 남는다")
				 .containsExactly(USER_1.key());
		}
	}

	@Nested
	@DisplayName("삭제가 실패하면")
	class WhenEvictionFails {

		@Test
		@DisplayName("실패 수를 캐시 이름과 함께 센다")
		void countsFailure() {
			MeterRegistry registry = new SimpleMeterRegistry();
			AfterCommitEvictor failing = new AfterCommitEvictor(
				 FailingCacheManager.of("redis down"),
				 new InvalidationMetrics(registry)
			);

			failing.evict(entriesOf(USER_1));

			assertThat(registry.find("cache.invalidation.failures").counter().count())
				 .as("로그만 남기면 집계하지 않는 한 stale 이 쌓이는 것을 아무도 모른다")
				 .isEqualTo(1);
		}
	}

	private void evict(CacheKey... entries) {
		evictor.evict(entriesOf(entries));
	}

	private static Set<CacheKey> entriesOf(CacheKey... entries) {
		return new LinkedHashSet<>(List.of(entries));
	}

	private void inTransaction(Runnable work) {
		inTransaction(status -> work.run());
	}

	private void inTransaction(Consumer<TransactionStatus> work) {
		new TransactionTemplate(transactionManager).executeWithoutResult(work);
	}

	private void inNewTransaction(Runnable work) {
		TransactionTemplate template = new TransactionTemplate(transactionManager);
		template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		template.executeWithoutResult(status -> work.run());
	}

	static class RecordingCacheManager extends ConcurrentMapCacheManager {

		private final List<String> evictedKeys = new ArrayList<>();

		RecordingCacheManager(String... cacheNames) {
			super(cacheNames);
		}

		List<String> evictedKeys() {
			return evictedKeys;
		}

		@Override
		protected Cache createConcurrentMapCache(String name) {
			return new ConcurrentMapCache(name) {
				@Override
				public void evict(Object key) {
					evictedKeys.add(String.valueOf(key));
					super.evict(key);
				}
			};
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
			return delegate.get(
				 key,
				 type
			);
		}

		@Override
		public <T> T get(Object key, Callable<T> valueLoader) {
			return delegate.get(
				 key,
				 valueLoader
			);
		}

		@Override
		public void put(Object key, Object value) {
			delegate.put(
				 key,
				 value
			);
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

	/**
	 * 중단·재개를 실제로 수행하는 최소 트랜잭션 관리자.
	 * <p>
	 * {@code REQUIRES_NEW} 가 바깥을 중단시키려면 관리자가 "진행 중인 트랜잭션이 있다" 를
	 * 알아야 하므로, 리소스 하나를 묶어 존재를 표시한다.
	 */
	static class StubTransactionManager extends AbstractPlatformTransactionManager {

		private static final Object RESOURCE_KEY = new Object();

		@Override
		protected Object doGetTransaction() {
			return new StubTransaction(TransactionSynchronizationManager.hasResource(RESOURCE_KEY));
		}

		@Override
		protected boolean isExistingTransaction(Object transaction) {
			return ((StubTransaction)transaction).active();
		}

		@Override
		protected void doBegin(Object transaction, TransactionDefinition definition) {
			TransactionSynchronizationManager.bindResource(
				 RESOURCE_KEY,
				 new Object()
			);
		}

		@Override
		protected Object doSuspend(Object transaction) {
			return TransactionSynchronizationManager.unbindResource(RESOURCE_KEY);
		}

		@Override
		protected void doResume(Object transaction, Object suspendedResources) {
			TransactionSynchronizationManager.bindResource(
				 RESOURCE_KEY,
				 suspendedResources
			);
		}

		@Override
		protected void doCommit(DefaultTransactionStatus status) {
		}

		@Override
		protected void doRollback(DefaultTransactionStatus status) {
		}

		@Override
		protected void doCleanupAfterCompletion(Object transaction) {
			TransactionSynchronizationManager.unbindResourceIfPossible(RESOURCE_KEY);
		}

		private record StubTransaction(boolean active) {
		}
	}
}
