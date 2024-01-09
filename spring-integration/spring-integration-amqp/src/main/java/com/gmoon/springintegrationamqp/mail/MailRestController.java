package com.gmoon.springintegrationamqp.mail;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gmoon.springintegrationamqp.config.amqp.AmqpMessageDestination;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/mail")
@RestController
@RequiredArgsConstructor
public class MailRestController {

	private final RabbitTemplate rabbitTemplate;

	@PostMapping("/send")
	public ResponseEntity<Void> send(String message) {
		log.info("send: {}", message);
		rabbitTemplate.convertAndSend(AmqpMessageDestination.SEND_EMAIL.value, message);
		return ResponseEntity.ok()
			.build();
	}
}
