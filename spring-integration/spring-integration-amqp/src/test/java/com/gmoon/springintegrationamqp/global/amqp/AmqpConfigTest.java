package com.gmoon.springintegrationamqp.global.amqp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AmqpConfigTest {

	@Disabled
	@Test
	void publish() {
		ConnectionFactory factory = newConnectionFactory();
		try (Connection conn = factory.newConnection()) {
			Channel channel = conn.createChannel();
			String queueName = declareQueue(channel, "queue1");

			String message = "Hello World!";
			channel.basicPublish("", queueName, null, message.getBytes(StandardCharsets.UTF_8));
			log.info("message: {}", message);
		} catch (Exception e) {
			log.error("", e);
		}
	}

	private String declareQueue(Channel channel, String queueName) throws IOException {
		channel.queueDeclare(queueName, true, false, false, null);
		return queueName;
	}

	private ConnectionFactory newConnectionFactory() {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		factory.setPort(5672);
		factory.setUsername("guest");
		factory.setPassword("guest");
		return factory;
	}
}
