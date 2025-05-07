package com.gmoon.springjpalock.global.lock;

import com.gmoon.springjpalock.global.AbstractJpaRepositoryTest;
import com.gmoon.springjpalock.global.Fixtures;
import com.gmoon.springjpalock.orders.domain.Order;
import com.mysql.cj.jdbc.exceptions.MySQLTransactionRollbackException;
import jakarta.persistence.LockModeType;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.RollbackException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.StaleStateException;
import org.hibernate.exception.LockAcquisitionException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@Disabled
class PessimisticLockTest extends AbstractJpaRepositoryTest {

	@DisplayName("S-Lock 교착 상태 검증"
		 + "[QUERY] select * from `tb_order` where `no`='order-no-001' lock in share mode"
		 + "[QUERY] update `tb_order` set `address`='d9884545-f76a-4bac-b952-44d6bb952358', `issued_count`=0, `status`='WAITING', `version`=2 where `no`='order-no-001' and `version`=1"
		 + "Caused by: com.mysql.jdbc.exceptions.jdbc4.MySQLTransactionRollbackException: Deadlock found when trying to get lock; try restarting transaction..."
		 + "https://dev.mysql.com/doc/refman/8.0/en/innodb-deadlock-example.html")
	@Test
	void sharedLock() {
		LockModeType sLock = LockModeType.PESSIMISTIC_READ;

		CompletableFuture<Void> allOf = CompletableFuture.allOf(
			 IntStream.range(0, 100)
				  .mapToObj(number -> CompletableFuture.runAsync(() -> saveWithLockMode(sLock)))
				  .toArray(CompletableFuture[]::new)
		);

		assertThatCode(allOf::join)
			 .isInstanceOf(CompletionException.class)
			 .cause().isInstanceOf(RollbackException.class)
			 .cause().isInstanceOf(OptimisticLockException.class)
			 .cause().isInstanceOf(LockAcquisitionException.class)
			 .cause().isInstanceOf(MySQLTransactionRollbackException.class);
	}

	@DisplayName("X-Lock 교착 상태 검증"
		 + "[QUERY] select * from `tb_order` order0_ where order0_.`no`='order-no-001' for update"
		 + "[QUERY] update `tb_order` set `address`='b9f14860-dd72-4a57-ad1d-9aa156a87f1b', `issued_count`=0, `status`='WAITING', `version`=3 where `no`='order-no-001' and `version`=2"
		 + "[QUERY] commit"
		 + "[QUERY] select * from `tb_order` order0_ where order0_.`no`='order-no-001' for update"
		 + "[QUERY] update `tb_order` set `address`='e09d2e17-8993-4906-86d7-88f74044c3a1', `issued_count`=0, `status`='WAITING', `version`=4 where `no`='order-no-001' and `version`=3"
		 + "[QUERY] commit"
	)
	@Test
	void exclusiveLock() {
		LockModeType xLock = LockModeType.PESSIMISTIC_WRITE;

		CompletableFuture<Void> allOf = CompletableFuture.allOf(
			 IntStream.range(0, 100)
				  .mapToObj(number -> CompletableFuture.runAsync(() -> saveWithLockMode(xLock)))
				  .toArray(CompletableFuture[]::new)
		);

		assertThatCode(allOf::join).doesNotThrowAnyException();
	}

	@DisplayName("X-Lock 트랜잭션이 종료되기 전엔 S-Lock을 획득할 수 없다.")
	@Test
	void exclusiveLockWithSLock() {
		LockModeType xLock = LockModeType.PESSIMISTIC_WRITE;

		CompletableFuture<Void> allOf = CompletableFuture.allOf(
			 CompletableFuture.runAsync(() -> saveWithLockMode(xLock)),
			 CompletableFuture.runAsync(() -> saveWithLockMode(xLock)),
			 CompletableFuture.runAsync(() -> saveWithLockMode(xLock)),
			 CompletableFuture.runAsync(() -> saveWithLockMode(LockModeType.PESSIMISTIC_READ)),
			 CompletableFuture.runAsync(() -> saveWithLockMode(LockModeType.PESSIMISTIC_READ)),
			 CompletableFuture.runAsync(() -> saveWithLockMode(LockModeType.PESSIMISTIC_READ)),
			 CompletableFuture.runAsync(() -> saveWithLockMode(xLock))
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

	private void saveWithLockMode(LockModeType lockMode) {
		executeQuery(em -> {
			Order order = em.find(Order.class, Fixtures.ORDER_NO, lockMode);
			order.changeAddress(UUID.randomUUID().toString());
			return em.merge(order);
		}, throwable -> log.error("Data conflict occurs.", throwable));
	}
}
