package com.gmoon.springeventlistener.simple;

import static org.mockito.BDDMockito.*;

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

		then(listener)
			.should(times(1))
			.onApplicationEvent(any());
	}
}
