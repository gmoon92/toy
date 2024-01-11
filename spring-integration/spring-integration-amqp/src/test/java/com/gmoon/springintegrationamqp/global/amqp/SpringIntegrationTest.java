package com.gmoon.springintegrationamqp.global.amqp;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SpringIntegrationTest {

	@Nested
	class MessageTest {

		@DisplayName("메시지는 header 와 payload 로 구성된다.")
		@Test
		void create() {
			Message<String> message = MessageBuilder.withPayload("hi")
				.setHeader("jwt", "uuid")
				.build();

			assertThat(message.getPayload()).isEqualTo("hi");
			assertThat(message.getHeaders().getId()).isNotNull();
			assertThat(message.getHeaders().getTimestamp()).isNotNull();
			assertThat(message.getHeaders().get("jwt")).isEqualTo("uuid");
		}
	}
}
