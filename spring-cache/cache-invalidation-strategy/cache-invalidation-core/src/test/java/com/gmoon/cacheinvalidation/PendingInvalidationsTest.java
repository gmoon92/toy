package com.gmoon.cacheinvalidation;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

@DisplayName("트랜잭션 범위 무효화 수집")
class PendingInvalidationsTest {

	private static final CacheKey USER_1 = CacheKey.of(TestCachePolicy.USER, 1L);
	private static final CacheKey USER_2 = CacheKey.of(TestCachePolicy.USER, 2L);

	private List<Set<CacheKey>> evicted;
	private PlatformTransactionManager transactionManager;

	@BeforeEach
	void setUp() {
		evicted = new ArrayList<>();
		transactionManager = new StubTransactionManager();
	}

	@Nested
	@DisplayName("트랜잭션이 없으면")
	class WhenNoTransaction {

		@Test
		@DisplayName("모으지 않고 즉시 지운다")
		void evictsImmediately() {
			collect(USER_1);

			assertThat(evictedKeys())
				 .as("모을 자리가 없는데 미루면 영영 지워지지 않는다")
				 .containsExactly(USER_1);
		}
	}

	@Nested
	@DisplayName("한 트랜잭션에서 같은 키를 여러 번 지목하면")
	class WhenSameKeyCollectedTwice {

		@Test
		@DisplayName("모으는 동안에는 지우지 않는다")
		void defersUntilCompletion() {
			inTransaction(() -> {
				collect(USER_1);
				collect(USER_1);

				assertThat(evicted)
					 .as("커밋 전에 지우면 롤백된 변경으로 멀쩡한 값을 버린다")
					 .isEmpty();
			});
		}

		@Test
		@DisplayName("커밋 이후 한 번만 지운다")
		void evictsOnceAfterCommit() {
			inTransaction(() -> {
				collect(USER_1);
				collect(USER_1);
			});

			assertThat(evictedKeys())
				 .as("신호원마다 즉시 지우면 같은 키를 두 번 지운다. 왕복과 지표가 두 배가 된다")
				 .containsExactly(USER_1);
		}
	}

	@Nested
	@DisplayName("트랜잭션이 롤백되면")
	class WhenRolledBack {

		@Test
		@DisplayName("모인 대상을 버린다")
		void discardsCollectedEntries() {
			inTransaction(status -> {
				collect(USER_1);
				status.setRollbackOnly();
			});

			assertThat(evicted)
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
				collect(USER_1);
				inNewTransaction(() -> collect(USER_2));

				assertThat(evictedKeys())
					 .as("안쪽은 이미 커밋됐다. 바깥이 끝나기를 기다리면 그만큼 stale 이 길어진다")
					 .containsExactly(USER_2);
			});
		}

		@Test
		@DisplayName("바깥이 롤백해도 안쪽에서 커밋된 키는 지워진다")
		void keepsInnerEvictionWhenOuterRollsBack() {
			inTransaction(status -> {
				collect(USER_1);
				inNewTransaction(() -> collect(USER_2));
				status.setRollbackOnly();
			});

			assertThat(evictedKeys())
				 .as("안쪽 키가 바깥 수집함에 섞이면 바깥 롤백이 이미 커밋된 변경의 무효화를 버린다")
				 .containsExactly(USER_2);
		}

		@Test
		@DisplayName("바깥이 커밋되면 바깥 키는 그때 지운다")
		void evictsOuterKeyOnOuterCommit() {
			inTransaction(() -> {
				collect(USER_1);
				inNewTransaction(() -> collect(USER_2));
			});

			assertThat(evictedKeys())
				 .as("수집함이 분리되면 안쪽이 먼저, 바깥이 나중에 지워진다")
				 .containsExactly(USER_2, USER_1);
		}
	}

	@Nested
	@DisplayName("트랜잭션이 끝나면")
	class WhenTransactionCompleted {

		@Test
		@DisplayName("수집함을 스레드에서 떼어 다음 트랜잭션에 새지 않게 한다")
		void unbindsCollector() {
			inTransaction(() -> collect(USER_1));

			assertThat(TransactionSynchronizationManager.getResourceMap())
				 .as("떼지 않으면 같은 스레드의 다음 트랜잭션이 옛 수집함을 이어받는다")
				 .isEmpty();
		}
	}

	private void collect(CacheKey entry) {
		PendingInvalidations.collect(
			 new LinkedHashSet<>(Set.of(entry)),
			 entries -> evicted.add(new LinkedHashSet<>(entries))
		);
	}

	private List<CacheKey> evictedKeys() {
		return evicted.stream().flatMap(Set::stream).toList();
	}

	private void inTransaction(Runnable work) {
		inTransaction(status -> work.run());
	}

	private void inTransaction(java.util.function.Consumer<org.springframework.transaction.TransactionStatus> work) {
		new TransactionTemplate(transactionManager).executeWithoutResult(work::accept);
	}

	private void inNewTransaction(Runnable work) {
		TransactionTemplate template = new TransactionTemplate(transactionManager);
		template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		template.executeWithoutResult(status -> work.run());
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
