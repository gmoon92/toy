package com.gmoon.springeventlistener.orders.order.events;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationEventPublisher;

@SpringBootTest
class OrderListenerTest {

	@Autowired
	private ApplicationEventPublisher publisher;

	@SpyBean
	private OrderListener listener;

	@DisplayName("어노테이션 기반 이벤트 처리")
	@Test
	void complete() {
		CompletedOrderEvent event = completedOrderEvent("0001");

		publisher.publishEvent(event);

		Awaitility.await()
			.pollDelay(Duration.ofSeconds(1))
			.atMost(Duration.ofSeconds(3))
			.untilAsserted(() -> then(listener)
				.should(times(1))
				.syncOrderLines(any()));
	}

	private CompletedOrderEvent completedOrderEvent(String orderNo) {
		return CompletedOrderEvent.builder()
			.orderNo(orderNo)
			.orderPrice(50_000)
			.productName("Clean Architecture")
			.userName("gmoon")
			.userEmail("gmoon0929@gmail.com")
			.build();
	}

	@DisplayName("주문 번호가 비어 있으면 이벤트 처리를 하지 않는다.")
	@ParameterizedTest
	@NullAndEmptySource
	void condition(String orderNo) {
		CompletedOrderEvent dto = completedOrderEvent(orderNo);

		publisher.publishEvent(dto);

		Awaitility.await()
			.pollDelay(Duration.ofSeconds(1))
			.atMost(Duration.ofSeconds(3))
			.untilAsserted(() -> then(listener)
				.should(never())
				.syncOrderLines(any()));
	}
}
