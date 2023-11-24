package com.gmoon.springjpalock.global.lock;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.persistence.LockModeType;
import javax.persistence.OptimisticLockException;
import javax.persistence.RollbackException;

import org.hibernate.StaleStateException;
import org.hibernate.exception.LockAcquisitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

import com.gmoon.springjpalock.global.BaseJpaTestCase;
import com.gmoon.springjpalock.global.Fixtures;
import com.gmoon.springjpalock.orders.domain.Order;
import com.mysql.cj.jdbc.exceptions.MySQLTransactionRollbackException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OptimisticLockTest extends BaseJpaTestCase {

	@DisplayName("@OptimisticLock 특정 컬럼을 데이터 충돌 검사에서 제외한다.")
	@RepeatedTest(5)
	void excludeLockColumns() {
		CompletableFuture<Void> allOf = CompletableFuture.allOf(
			CompletableFuture.runAsync(this::issueReceipt),
			CompletableFuture.runAsync(this::issueReceipt),
			CompletableFuture.runAsync(this::issueReceipt),
			CompletableFuture.runAsync(this::issueReceipt),
			CompletableFuture.runAsync(this::issueReceipt)
		);

		assertThatCode(allOf::join)
			.doesNotThrowAnyException();
	}

	private void issueReceipt() {
		executeQuery(em -> {
			Order order = em.find(Order.class, Fixtures.ORDER_NO, LockModeType.OPTIMISTIC);
			order.issue();
			return em.merge(order);
		}, RuntimeException::new);
	}

	@DisplayName("데이터 충돌 발생되면 OptimisticLockException 예외가 발생한다.")
	@RepeatedTest(5)
	void runRaceConditionByOptimisticLock() {
		CompletableFuture<Void> allOf = CompletableFuture.allOf(
			CompletableFuture.runAsync(this::updateOrderInfos),
			CompletableFuture.runAsync(this::updateOrderInfos),
			CompletableFuture.runAsync(this::updateOrderInfos),
			CompletableFuture.runAsync(this::updateOrderInfos)
		);

		assertThatThrownBy(allOf::join)
			.getCause()
			.isInstanceOfAny(
				LockAcquisitionException.class,
				RollbackException.class,
				OptimisticLockException.class,
				StaleStateException.class,
				MySQLTransactionRollbackException.class
			);
	}

	private void updateOrderInfos() {
		executeQuery(em -> {
			Order order = em.find(Order.class, Fixtures.ORDER_NO, LockModeType.OPTIMISTIC);
			order.changeAddress(UUID.randomUUID().toString());
			return em.merge(order);
		}, throwable -> log.error("Data conflict occurs.", throwable));
	}
}
