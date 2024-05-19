package com.gmoon.springeventlistener.global.events.simple;

import static org.mockito.BDDMockito.*;

import java.time.Duration;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

@SpringBootTest
class CustomEventListenerTest {

	@Autowired
	private CustomEventPublisher publisher;

	@SpyBean
	private CustomEventListener listener;

	@DisplayName("이벤트 발행 및 감지")
	@Test
	void onApplicationEvent() {
		publisher.raise("event1");

		Awaitility.await()
			 .pollDelay(Duration.ofSeconds(1))
			 .atMost(Duration.ofSeconds(3))
			 .untilAsserted(() -> then(listener)
				  .should(times(1))
				  .onApplicationEvent(any()));
	}
}
