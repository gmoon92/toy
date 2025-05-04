package com.gmoon.springkafkabroker.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.kafka.ConfluentKafkaContainer;

/**
 * @see <a href="https://java.testcontainers.org/modules/kafka/">testcontainers kafka</a>
 * */
@Slf4j
public class KafkaTestContainerExtension
	 implements BeforeAllCallback, AfterAllCallback {

	@Container
	public static final ConfluentKafkaContainer KAFKA_CONTAINER = new ConfluentKafkaContainer("confluentinc/cp-kafka:7.4.0");

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		log.info("[Testcontainers-kafka] start.");
		if (!KAFKA_CONTAINER.isRunning()) {
			KAFKA_CONTAINER.start();
			log.info("[Testcontainers-kafka] bootstrap.servers: {}", KAFKA_CONTAINER.getBootstrapServers());
		}
		log.info("[Testcontainers-kafka] end.");
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
		KAFKA_CONTAINER.stop();
	}
}
