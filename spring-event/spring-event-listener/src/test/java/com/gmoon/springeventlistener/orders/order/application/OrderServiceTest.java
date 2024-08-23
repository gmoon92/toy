package com.gmoon.springeventlistener.orders.order.application;

import static com.gmoon.springeventlistener.orders.Fixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import com.gmoon.springeventlistener.orders.order.domain.Order;
import com.gmoon.springeventlistener.orders.order.domain.OrderStatus;
import com.gmoon.springeventlistener.orders.order.events.OrderListener;

@SpringBootTest
class OrderServiceTest {

	@Autowired
	private OrderService service;

	@SpyBean
	private OrderListener listener;

	@Test
	void complete() {
		Order order = new Order(OrderStatus.ACCEPTED, user())
			 .addOrderItems(
				  orderLineItem(1, product("p1", 16_000)),
				  orderLineItem(1, product("p2", 36_000))
			 );

		service.complete(order);

		assertAll(
			 () -> assertThat(order.getNo()).isNotBlank(),
			 () -> assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED)
		);
		Awaitility.await()
			 .pollDelay(Duration.ofSeconds(1))
			 .atMost(Duration.ofSeconds(3))
			 .untilAsserted(() -> then(listener)
				  .should(times(1))
				  .syncOrderLines(any()));
	}

	@DisplayName("프로덕션 코드 예외 발생시 리스너는 수행하지 않는다.")
	@Test
	void error1() {
		Order order = new Order(OrderStatus.WAITE, user())
			 .addOrderItems(
				  orderLineItem(1, product("p1", 16_000)),
				  orderLineItem(1, product("p2", 36_000))
			 );

		assertThatExceptionOfType(IllegalStateException.class)
			 .isThrownBy(() -> service.complete(order));
		Awaitility.await()
			 .pollDelay(Duration.ofSeconds(1))
			 .atMost(Duration.ofSeconds(3))
			 .untilAsserted(() -> then(listener)
				  .should(never())
				  .syncOrderLines(any()));
	}
}
