package com.gmoon.springintegrationamqp.global.amqp;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.util.UriComponentsBuilder;

import com.gmoon.springintegrationamqp.mail.application.MailService;
import com.gmoon.springintegrationamqp.mail.model.MailMessage;
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

		@Bean
		public MessageChannel mailChannel() {
			return new DirectChannel();
		}

		/***
		 * inboundSendMail -> outboundSendMail -> inboundLogMail
		 */
		@Bean
		public IntegrationFlow inSendMail(ConnectionFactory connectionFactory, MessageChannel mailChannel) {
			return IntegrationFlows
				.from(Amqp.inboundAdapter(connectionFactory, AmqpMessageDestination.SEND_MAIL.value))
				.log(LoggingHandler.Level.DEBUG)
				.channel(mailChannel)
				.transform(Transformers.fromStream(StandardCharsets.UTF_8.name()))
				.handle(mailService, "send")
				.get();
		}


		/**
		 * https://docs.spring.io/spring-integration/reference/amqp/outbound-channel-adapter.html
		 * */
		@Bean
		public IntegrationFlow outSendMail(AmqpTemplate amqpTemplate, MessageChannel mailChannel) {
			return IntegrationFlows
				.from(mailChannel)
				.log(LoggingHandler.Level.DEBUG)
				.handle(Amqp.outboundAdapter(amqpTemplate)
					.routingKey(AmqpMessageDestination.WELCOME_MAIL.value)
				)
				.get();
		}

		@Bean
		public IntegrationFlow inWelcomeMail(ConnectionFactory connectionFactory) {
			return IntegrationFlows
				.from(Amqp.inboundAdapter(connectionFactory, AmqpMessageDestination.WELCOME_MAIL.value))
				.log(LoggingHandler.Level.DEBUG)
				.convert(MailMessage.Payload.class)
				.handle(mailService, "welcome")
				.get();
		}
	}
}
