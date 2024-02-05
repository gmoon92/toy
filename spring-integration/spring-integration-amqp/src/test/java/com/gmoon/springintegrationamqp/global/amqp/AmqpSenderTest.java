package com.gmoon.springintegrationamqp.global.amqp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.gmoon.springintegrationamqp.mail.model.SendMailVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
class AmqpSenderTest {

	@Autowired
	private AmqpSender amqpSender;

	@Test
	void send() {
		amqpSender.send(
			AmqpMessageDestination.SEND_MAIL.value,
			SendMailVO.builder()
				.to("gmoon92@gmail.com")
				.from("system@email.com")
				.subject("subject")
				.content("welcome")
				.build()
		);
	}
}
