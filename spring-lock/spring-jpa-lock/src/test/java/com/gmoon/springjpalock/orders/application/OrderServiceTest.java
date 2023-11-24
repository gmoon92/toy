package com.gmoon.springjpalock.orders.application;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.hibernate.exception.LockAcquisitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.gmoon.springjpalock.global.Fixtures;
import com.gmoon.springjpalock.orders.domain.Order;
import com.gmoon.springjpalock.orders.domain.OrderLineItem;
import com.mysql.cj.jdbc.exceptions.MySQLTransactionRollbackException;

@SpringBootTest
class OrderServiceTest {

	@Autowired
	private OrderService service;

	@DisplayName(
		"데드락 검증"
		+ "org.hibernate.exception.LockAcquisitionException: could not execute statement"
		+ "Caused by: com.mysql.cj.jdbc.exceptions.MySQLTransactionRollbackException: Deadlock found when trying to get lock; try restarting transaction"
		+ "1. TX-A 주문 생성(INSERT)시 Menu ID 를 포함함으로, 해당 Menu 테이블의 ROW 에 대해 S-Lock 획득"
		+ "2. TX-B 주문 생성(INSERT)시 Menu ID 를 포함함으로, 해당 Menu 테이블의 ROW 에 대해 S-Lock 획득"
		+ "3. TX-A 메뉴 제고 감소 시도(UPDATE ~ WHERE), 해당 Menu 테이블의 ROW에 대해 X-Lock 획득 시도, TX-B 트랜잭션 종료 대기"
		+ "4. TX-B 메뉴 제고 감소 시도(UPDATE ~ WHERE), 해당 Menu 테이블의 ROW에 대해 X-Lock 획득 시도, TX-A 트랜잭션 종료 대기"
		+ "5. Deadlock!!!"
	)
	@Test
	void deadLock() {
		CompletableFuture<Void> allOf = CompletableFuture.allOf(
			CompletableFuture.runAsync(this::ordering),
			CompletableFuture.runAsync(this::ordering)
		);

		assertThatCode(allOf::join)
			.getCause()
			.getCause().isInstanceOf(LockAcquisitionException.class)
			.getCause().isInstanceOf(MySQLTransactionRollbackException.class);
	}

	private void ordering() {
		Order order = Fixtures.newOrder(getNewOrderLineItems());

		String menuId = "menu-001";
		service.ordering(order, menuId);
	}

	private List<OrderLineItem> getNewOrderLineItems() {
		return IntStream.range(0, 100)
			.mapToObj(this::newOrderLineTime)
			.collect(Collectors.toList());
	}

	private OrderLineItem newOrderLineTime(int quantity) {
		return OrderLineItem.builder()
			.quantity(quantity)
			.build();
	}
}
