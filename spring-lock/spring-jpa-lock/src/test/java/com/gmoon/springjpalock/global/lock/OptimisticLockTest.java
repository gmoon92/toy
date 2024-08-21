package com.gmoon.springjpalock.global.lock;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gmoon.springjpalock.global.BaseJpaTestCase;
import com.gmoon.springjpalock.global.Fixtures;
import com.gmoon.springjpalock.orders.domain.Order;
import com.mysql.cj.jdbc.exceptions.MySQLTransactionRollbackException;
import jakarta.persistence.LockModeType;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.RollbackException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.StaleStateException;
import org.hibernate.exception.LockAcquisitionException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Slf4j
@Disabled
class OptimisticLockTest extends BaseJpaTestCase {

	@DisplayName("@OptimisticLock 특정 컬럼을 데이터 충돌 검사에서 제외한다.")
	@Test
	void excludeLockColumns() {
		CompletableFuture<Void> allOf = CompletableFuture.allOf(
			 IntStream.range(0, 100)
				  .mapToObj(number -> CompletableFuture.runAsync(this::issueReceipt))
				  .toArray(CompletableFuture[]::new)
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
	@Test
	void runRaceConditionByOptimisticLock() {
		CompletableFuture<Void> allOf = CompletableFuture.allOf(
			 IntStream.range(0, 100)
				  .mapToObj(number -> CompletableFuture.runAsync(this::updateOrderInfos))
				  .toArray(CompletableFuture[]::new)
		);

		assertThatThrownBy(allOf::join)
			 .cause()
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
