package com.gmoon.springjpalock.orders;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.springjpalock.orders.domain.Order;
import com.gmoon.springjpalock.orders.domain.OrderRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@DataJpaTest
class OrderRepositoryTest {

	private final String orderNo = "order-no-001";

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
			CompletableFuture.runAsync(() -> repository.findByNoWithSharedLock(orderNo)),
			CompletableFuture.runAsync(() -> repository.findByNoWithSharedLock(orderNo)),
			CompletableFuture.runAsync(() -> repository.findByNoWithSharedLock(orderNo)),
			CompletableFuture.runAsync(() -> repository.findByNoWithSharedLock(orderNo))
		);

		assertThatCode(allOf::join)
			.doesNotThrowAnyException();
	}

	@DisplayName("X-Lock"
		+ "for update"
		+ "[QUERY] select * from `tb_order` where `no`='order-no-001' for update")
	@Test
	void readWithXLock() {
		CompletableFuture<Void> allOf = CompletableFuture.allOf(
			CompletableFuture.runAsync(() -> repository.findByNoWithExclusiveLock(orderNo)),
			CompletableFuture.runAsync(() -> repository.findByNoWithExclusiveLock(orderNo)),
			CompletableFuture.runAsync(() -> repository.findByNoWithExclusiveLock(orderNo)),
			CompletableFuture.runAsync(() -> repository.findByNoWithExclusiveLock(orderNo))
		);

		assertThatCode(allOf::join)
			.doesNotThrowAnyException();
	}

	@AfterEach
	void tearDown() {
		repository.flush();
	}
}
