package com.gmoon.springintegrationamqp.global.amqp;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.UriComponentsBuilder;

import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.ClientParameters;

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
}
