package com.gmoon.springintegrationamqp.global.amqp;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.web.util.UriComponentsBuilder;

import com.gmoon.springintegrationamqp.mail.application.MailService;
import com.gmoon.springintegrationamqp.mail.model.SaveMailLogVO;
import com.rabbitmq.client.Channel;
import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.ClientParameters;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @see RabbitProperties
 * @see RabbitAutoConfiguration
 */
@Slf4j
@Configuration
public class AmqpConfig {

	/**
	 * spring.rabbitmq.dynamic 옵션이 활성화되면
	 * {@link AmqpAdmin} 에 의해 등록된 {@link Queue} 빈을 기준으로 브로커에 큐 생성
	 */
	@Bean(initMethod = "declareQueues", destroyMethod = "deleteGarbageQueues")
	public AmqpUtils amqpUtils(AmqpAdmin amqpAdmin, RabbitTemplate rabbitTemplate, Client client) {
		return new AmqpUtils(rabbitTemplate, amqpAdmin, client);
	}

	/**
	 * https://github.com/rabbitmq/hop
	 */
	@Bean
	public Client client(RabbitProperties properties) throws MalformedURLException, URISyntaxException {
		return new Client(new ClientParameters()
			.url(UriComponentsBuilder.newInstance()
				.scheme("http")
				.host(properties.getHost())
				.path("api")
				.port("1" + properties.getPort())
				.toUriString())
			.username(properties.getUsername())
			.password(properties.getPassword()));
	}

	@Configuration
	@RequiredArgsConstructor
	protected static class MailFlowConfig {

		private final MailService mailService;

		/***
		 * inboundSendMail -> outboundSendMail -> inboundLogMail
		 */
		@Bean
		public IntegrationFlow inSendMail(ConnectionFactory connectionFactory) {
			String queueName = AmqpMessageDestination.SEND_MAIL.value;
			return IntegrationFlows
				.from(Amqp.inboundAdapter(connectionFactory, queueName))
				.handle(mailService, "send") // @ServiceActivator
				.channel(MessageChannels.direct(queueName)) // channing 순서도 중요.
				.log(LoggingHandler.Level.DEBUG)
				.get();
		}

		/**
		 * https://docs.spring.io/spring-integration/reference/amqp/outbound-channel-adapter.html
		 */
		@Bean
		public IntegrationFlow outSendMail(AmqpTemplate amqpTemplate, IntegrationFlow inSendMail) {
			return IntegrationFlows
				.from(inSendMail.getInputChannel())
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
			return IntegrationFlows
				.from(Amqp.inboundAdapter(connectionFactory, routingKey))
				.transform(SaveMailLogVO::of)
				.handle(mailService, "saveLog")  // @ServiceActivator
				.log(LoggingHandler.Level.DEBUG)
				.get();
		}

	}
}
