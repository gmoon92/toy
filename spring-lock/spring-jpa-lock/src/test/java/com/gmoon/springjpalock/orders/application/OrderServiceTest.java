package com.gmoon.springjpalock.orders.application;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import org.hibernate.exception.LockAcquisitionException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import com.gmoon.springjpalock.global.Fixtures;
import com.gmoon.springjpalock.orders.domain.Order;
import com.gmoon.springjpalock.orders.domain.OrderLineItem;
import com.mysql.cj.jdbc.exceptions.MySQLTransactionRollbackException;

@Disabled
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
			 .cause()
			 .cause().isInstanceOf(LockAcquisitionException.class)
			 .cause().isInstanceOf(MySQLTransactionRollbackException.class);
	}

	private void ordering() {
		Order order = Fixtures.newOrder(getNewOrderLineItems());

		String menuId = "menu-001";
		service.ordering(order, menuId);
	}

	private List<OrderLineItem> getNewOrderLineItems() {
		return IntStream.range(0, 100)
			 .mapToObj(this::newOrderLineTime)
			 .toList();
	}

	private OrderLineItem newOrderLineTime(int quantity) {
		return OrderLineItem.builder()
			 .quantity(quantity)
			 .build();
	}

	@DisplayName("동시성 이슈 검증")
	@Rollback
	@Test
	void issueReceipt() {
		CompletableFuture.allOf(
			 IntStream.range(0, 1_000)
				  .mapToObj(number -> CompletableFuture.runAsync(this::incrementIssuedCount))
				  .toArray(CompletableFuture[]::new)
		).join();

		Order order = service.getOrder(Fixtures.ORDER_NO);

		assertThat(order)
			 .hasFieldOrPropertyWithValue("issuedCount", 1_000L)
			 .isNotNull();
	}

	private void incrementIssuedCount() {
		service.issueReceipt(Fixtures.ORDER_NO);
	}
}
