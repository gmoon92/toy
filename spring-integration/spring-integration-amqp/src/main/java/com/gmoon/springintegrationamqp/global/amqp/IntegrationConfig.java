package com.gmoon.springintegrationamqp.global.amqp;

import java.util.List;
import java.util.Map;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.handler.LoggingHandler;

import com.rabbitmq.client.Channel;

import com.gmoon.springintegrationamqp.mail.application.MailService;
import com.gmoon.springintegrationamqp.mail.model.SaveMailLogVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
class IntegrationConfig {

	private final MailService mailService;

	/***
	 * inboundSendMail -> outboundSendMail -> inboundLogMail
	 */
	@Bean
	public IntegrationFlow inSendMail(ConnectionFactory connectionFactory) {
		String queueName = AmqpMessageDestination.SEND_MAIL.value;
		return IntegrationFlow
			 .from(Amqp.inboundAdapter(connectionFactory, queueName))
			 .log(LoggingHandler.Level.DEBUG)
			 .channel(MessageChannels.direct(queueName)) // channing 순서도 중요.
			 .handle(mailService, "send") // @ServiceActivator
			 .get();
	}

	/**
	 * https://docs.spring.io/spring-integration/reference/amqp/outbound-channel-adapter.html
	 */
	@Bean
	public IntegrationFlow outSendMail(AmqpTemplate amqpTemplate) {
		return IntegrationFlow
			 .from(AmqpMessageDestination.SEND_MAIL.value)
			 .handle(
				  Amqp.outboundAdapter(amqpTemplate)
					   .routingKey(AmqpMessageDestination.SAVE_MAIL_LOG.value)
			 )
			 .get();
	}

	/**
	 * @see AmqpInboundChannelAdapter.Listener#createMessageFromAmqp(Message, Channel)
	 * @see AmqpInboundChannelAdapter.Listener#createMessageFromPayload(Object, Channel, Map, long, List)
	 */
	@Bean
	public IntegrationFlow inSaveMailLog(ConnectionFactory connectionFactory) {
		String routingKey = AmqpMessageDestination.SAVE_MAIL_LOG.value;
		return IntegrationFlow
			 .from(Amqp.inboundAdapter(connectionFactory, routingKey))
			 .log(LoggingHandler.Level.DEBUG)
			 .transform(SaveMailLogVO::of)
			 .handle(mailService, "saveLog")  // @ServiceActivator
			 .get();
	}

	@Bean
	public AmqpSender amqpSender(BeanFactory beanFactory) {
		return new AmqpSender(beanFactory);
	}
}
