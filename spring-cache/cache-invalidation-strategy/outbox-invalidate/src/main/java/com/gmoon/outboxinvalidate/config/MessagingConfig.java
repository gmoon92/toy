package com.gmoon.outboxinvalidate.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 무효화 지시가 지나가는 큐를 선언한다.
 * <p>
 * 브로커가 재시작해도 지시가 남도록 큐를 durable 로 둔다. Spring AMQP 는 메시지를 기본으로 영속 전송한다.
 */
@Configuration
public class MessagingConfig {

	@Bean
	public Queue evictionQueue(ServiceCacheProperties properties) {
		return QueueBuilder.durable(properties.outbox().queue()).build();
	}

	@Bean
	public MessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}
}
