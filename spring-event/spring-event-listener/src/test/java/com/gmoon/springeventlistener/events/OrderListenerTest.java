package com.gmoon.springeventlistener.events;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

import java.time.Duration;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
		CompletedOrderEvent dto = CompletedOrderEvent.builder()
			.orderNo("0001")
			.orderPrice(50_000)
			.productName("Clean Architecture")
			.userName("gmoon")
			.userEmail("gmoon0929@gmail.com")
			.build();

		publisher.publishEvent(dto);

		Awaitility.await()
			.pollDelay(Duration.ofSeconds(1))
			.atMost(Duration.ofSeconds(3))
			.untilAsserted(() -> then(listener)
				.should(times(1))
				.complete(any()));
	}
}
