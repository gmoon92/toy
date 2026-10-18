package com.gmoon.cacheinvalidation;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
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

@DisplayName("커밋 이후 캐시 삭제")
class AfterCommitEvictorTest {

	private static final InvalidationMetrics NO_METRICS = new InvalidationMetrics(null);

	private static final CacheKey USER_1 = CacheKey.of(TestCachePolicy.USER, 1L);
	private static final CacheKey USER_2 = CacheKey.of(TestCachePolicy.USER, 2L);
	private static final CacheKey SUMMARY_1 = CacheKey.of(TestCachePolicy.USER_SUMMARY, 1L);
	private static final Executor CALLER_THREAD = Runnable::run;

	private RecordingCacheManager caches;
	private AfterCommitEvictor evictor;
	private PlatformTransactionManager transactionManager;

	@BeforeEach
	void setUp() {
		caches = new RecordingCacheManager(TestCachePolicy.Name.USER);
		evictor = new AfterCommitEvictor(
			 caches,
			 CALLER_THREAD,
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
		@DisplayName("모으는 동안에는 지우지 않는다")
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
		@DisplayName("커밋 이후 한 번만 지운다")
		void evictsOnceAfterCommit() {
			inTransaction(() -> {
				evict(USER_1);
				evict(USER_1);
			});

			assertThat(caches.evictedKeys())
				 .as("신호원마다 즉시 지우면 같은 키를 두 번 지운다. 왕복과 지표가 두 배가 된다")
				 .containsExactly(USER_1.key());
		}
	}

	@Nested
	@DisplayName("트랜잭션이 롤백되면")
	class WhenRolledBack {

		@Test
		@DisplayName("모인 대상을 버린다")
		void discardsCollectedEntries() {
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
	@DisplayName("REQUIRES_NEW 안쪽에서 수집하면")
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
				 .as("안쪽 키가 바깥 수집함에 섞이면 바깥 롤백이 이미 커밋된 변경의 무효화를 버린다")
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
				 .as("수집함이 분리되면 안쪽이 먼저, 바깥이 나중에 지워진다")
				 .containsExactly(USER_2.key(), USER_1.key());
		}
	}

	@Nested
	@DisplayName("트랜잭션이 끝나면")
	class WhenTransactionCompleted {

		@Test
		@DisplayName("수집함을 스레드에서 떼어 다음 트랜잭션에 새지 않게 한다")
		void unbindsCollector() {
			inTransaction(() -> evict(USER_1));

			assertThat(TransactionSynchronizationManager.getResourceMap())
				 .as("떼지 않으면 같은 스레드의 다음 트랜잭션이 옛 수집함을 이어받는다")
				 .isEmpty();
		}
	}

	@Nested
	@DisplayName("한 트랜잭션에 삭제기가 둘이면")
	class WhenTwoEvictorsShareTransaction {

		@Test
		@DisplayName("각자 자기 캐시 관리자로 지운다")
		void keepsCollectorsApart() {
			RecordingCacheManager otherCaches = new RecordingCacheManager(TestCachePolicy.Name.USER);
			AfterCommitEvictor other = new AfterCommitEvictor(
				 otherCaches,
				 CALLER_THREAD,
				 NO_METRICS
			);

			inTransaction(() -> {
				evict(USER_1);
				other.evict(entriesOf(USER_2));
			});

			assertThat(otherCaches.evictedKeys())
				 .as("수집함 키를 클래스로 잡으면 나중 삭제기의 키가 먼저 삭제기의 캐시 관리자로 지워진다")
				 .containsExactly(USER_2.key());
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
			caches.getCache(TestCachePolicy.Name.USER).put(USER_1.key(), "cached");

			evict(USER_1);

			assertThat(caches.getCache(TestCachePolicy.Name.USER).get(USER_1.key()))
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
			evict(SUMMARY_1, USER_1);

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
				 CALLER_THREAD,
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
			given(partlyFailing.getCache(TestCachePolicy.Name.USER_SUMMARY))
				 .willReturn(new EvictThrowingCache(new ConcurrentMapCache(TestCachePolicy.Name.USER_SUMMARY)));
			given(partlyFailing.getCache(TestCachePolicy.Name.USER))
				 .willReturn(caches.getCache(TestCachePolicy.Name.USER));
			AfterCommitEvictor evictorOverFailure = new AfterCommitEvictor(
				 partlyFailing,
				 CALLER_THREAD,
				 NO_METRICS
			);

			evictorOverFailure.evict(entriesOf(SUMMARY_1, USER_1));

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
				 CALLER_THREAD,
				 new InvalidationMetrics(registry)
			);

			failing.evict(entriesOf(USER_1));

			assertThat(registry.find("cache.invalidation.failures").counter().count())
				 .as("로그만 남기면 집계하지 않는 한 stale 이 쌓이는 것을 아무도 모른다")
				 .isEqualTo(1);
		}
	}

	@Nested
	@DisplayName("실행기가 삭제를 나중에 돌리면")
	class WhenExecutorDefers {

		private List<Runnable> queued;
		private AfterCommitEvictor deferring;

		@BeforeEach
		void setUp() {
			queued = new ArrayList<>();
			deferring = new AfterCommitEvictor(
				 caches,
				 queued::add,
				 NO_METRICS
			);
		}

		@Test
		@DisplayName("커밋한 스레드에서는 지우지 않는다")
		void leavesCommitThreadFree() {
			inTransaction(() -> deferring.evict(entriesOf(USER_1)));

			assertThat(caches.evictedKeys())
				 .as("커밋 스레드가 Redis 왕복을 기다리면 응답 지연이 키 개수만큼 늘어난다")
				 .isEmpty();
		}

		@Test
		@DisplayName("실행기가 돌면 커밋 때 모인 키를 지운다")
		void evictsWhenExecutorRuns() {
			inTransaction(() -> deferring.evict(entriesOf(USER_1, USER_2)));

			queued.forEach(Runnable::run);

			assertThat(caches.evictedKeys())
				 .as("실행기로 넘긴 뒤에도 모은 키가 하나도 빠지면 안 된다")
				 .containsExactly(USER_1.key(), USER_2.key());
		}

		@Test
		@DisplayName("넘긴 뒤 원본이 바뀌어도 넘긴 대상만 지운다")
		void evictsSnapshotOnly() {
			Set<CacheKey> entries = entriesOf(USER_1);
			deferring.evict(entries);
			entries.add(USER_2);

			queued.forEach(Runnable::run);

			assertThat(caches.evictedKeys())
				 .as("호출자의 컬렉션을 그대로 넘기면 실행 시점의 내용이 지워진다")
				 .containsExactly(USER_1.key());
		}
	}

	@Nested
	@DisplayName("실행기가 돌기 전에 애플리케이션이 내려가면")
	class WhenShutdownBeforeExecutorRuns {

		private final List<Runnable> queued = new ArrayList<>();
		private AfterCommitEvictor deferring;

		@BeforeEach
		void setUp() {
			deferring = new AfterCommitEvictor(caches, queued::add, NO_METRICS);
		}

		@Test
		@DisplayName("큐에 남은 삭제를 종료 스레드에서 마저 지운다")
		void drainsWhatExecutorNeverRan() {
			inTransaction(() -> deferring.evict(entriesOf(USER_1, USER_2)));
			assertThat(caches.evictedKeys()).isEmpty();

			deferring.drainPending();

			assertThat(caches.evictedKeys())
				 .as("실행기에 넘긴 채로 프로세스가 내려가면 그 키는 TTL 이 끝날 때까지 stale 로 남는다")
				 .containsExactly(USER_1.key(), USER_2.key());
		}

		@Test
		@DisplayName("실행기가 이미 지운 것은 두 번 지우지 않는다")
		void doesNotEvictTwice() {
			inTransaction(() -> deferring.evict(entriesOf(USER_1)));
			queued.forEach(Runnable::run);

			deferring.drainPending();

			assertThat(caches.evictedKeys())
				 .as("종료 처리가 실행기와 겹쳐 같은 키를 두 번 지우면 왕복만 늘어난다")
				 .containsExactly(USER_1.key());
		}

		@Test
		@DisplayName("지울 것이 없으면 아무 일도 하지 않는다")
		void drainsNothingWhenIdle() {
			deferring.drainPending();

			assertThat(caches.evictedKeys()).isEmpty();
		}
	}

	@Nested
	@DisplayName("실행기가 작업을 거부하면")
	class WhenExecutorRejects {

		@Test
		@DisplayName("호출한 스레드에서 직접 지운다")
		void evictsOnCallerThread() {
			AfterCommitEvictor rejecting = new AfterCommitEvictor(
				 caches,
				 task -> {
					 throw new RejectedExecutionException("queue full");
				 },
				 NO_METRICS
			);

			inTransaction(() -> rejecting.evict(entriesOf(USER_1)));

			assertThat(caches.evictedKeys())
				 .as("큐가 찼다고 버리면 TTL 이 끝날 때까지 옛 값이 남는다")
				 .containsExactly(USER_1.key());
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
