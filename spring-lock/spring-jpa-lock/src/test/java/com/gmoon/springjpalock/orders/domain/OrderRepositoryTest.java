package com.gmoon.springjpalock.orders.domain;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.gmoon.springjpalock.global.BaseJpaTestCase;
import com.gmoon.springjpalock.global.Fixtures;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Disabled
class OrderRepositoryTest extends BaseJpaTestCase {

	@Autowired
	private OrderRepository repository;

	@Test
	void findAll() {
		assertThatCode(() -> repository.findAll())
			 .doesNotThrowAnyException();
	}

	@Test
	void save() {
		String address = "Seoul";
		Order newOrder = new Order(address);

		Order mergedOrder = repository.saveAndFlush(newOrder);

		mergedOrder.changeAddress(address + LocalDateTime.now());
	}

	@DisplayName("S-Lock"
		 + "lock in share mode"
		 + "[QUERY] select * from `tb_order` where `no`='order-no-001' lock in share mode")
	@Test
	@Transactional
	void readWithSLock() {
		CompletableFuture<Void> allOf = CompletableFuture.allOf(
			 CompletableFuture.runAsync(() -> repository.findByNoWithSharedLock(Fixtures.ORDER_NO)),
			 CompletableFuture.runAsync(() -> repository.findByNoWithSharedLock(Fixtures.ORDER_NO)),
			 CompletableFuture.runAsync(() -> repository.findByNoWithSharedLock(Fixtures.ORDER_NO)),
			 CompletableFuture.runAsync(() -> repository.findByNoWithSharedLock(Fixtures.ORDER_NO))
		);

		assertThatCode(allOf::join)
			 .doesNotThrowAnyException();
	}

	//	https://dev.mysql.com/doc/refman/8.4/en/innodb-performance-ro-txn.html
	@DisplayName("X-Lock"
		 + "for update"
		 + "[QUERY] select * from `tb_order` where `no`='order-no-001' for update")
	@Test
	void readWithXLock() {
		CompletableFuture<Void> allOf = CompletableFuture.allOf(
			 CompletableFuture.runAsync(() -> repository.findByNoWithExclusiveLock(Fixtures.ORDER_NO)),
			 CompletableFuture.runAsync(() -> repository.findByNoWithExclusiveLock(Fixtures.ORDER_NO)),
			 CompletableFuture.runAsync(() -> repository.findByNoWithExclusiveLock(Fixtures.ORDER_NO)),
			 CompletableFuture.runAsync(() -> repository.findByNoWithExclusiveLock(Fixtures.ORDER_NO))
		);

		Assertions.setPrintAssertionsDescription(true);
		Assertions.assertThatThrownBy(allOf::join)
			 .hasCauseInstanceOf(JpaSystemException.class);
	}

	@AfterEach
	void tearDown() {
		repository.flush();
	}
}
