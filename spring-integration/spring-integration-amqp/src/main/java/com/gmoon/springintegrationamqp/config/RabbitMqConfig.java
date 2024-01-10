package com.gmoon.springintegrationamqp.config;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.web.util.UriComponentsBuilder;

import com.gmoon.springintegrationamqp.config.amqp.AmqpMessageDestination;
import com.gmoon.springintegrationamqp.config.amqp.AmqpUtils;
import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.ClientParameters;

import lombok.extern.slf4j.Slf4j;

/**
 * @see RabbitProperties
 * @see RabbitAutoConfiguration
 */
@Slf4j
@Configuration
public class RabbitMqConfig {


	/**
	 * spring.rabbitmq.dynamic 옵션이 활성화되면
	 * {@link AmqpAdmin} 에 의해 등록된 {@link Queue} 빈을 기준으로 브로커에 큐 생성
	 */
	@Bean(initMethod = "declareQueues", destroyMethod = "deleteGarbageQueues")
	public AmqpUtils amqpUtils(AmqpAdmin amqpAdmin, Client client) {
		return new AmqpUtils(amqpAdmin, client);
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
	protected static class ChannelConfig {

		@Bean
		public IntegrationFlow inboundSendMail(ConnectionFactory connectionFactory) {
			return IntegrationFlows
				.from(Amqp.inboundAdapter(connectionFactory, AmqpMessageDestination.SEND_EMAIL.value))
				.log(LoggingHandler.Level.DEBUG)
				.handle(message -> {
					log.info("in headers: {}", message.getHeaders());
					log.info("in message: {}", message.getPayload());
				})
				.get();
		}
	}
}
