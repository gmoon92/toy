package com.gmoon.outboxinvalidate;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import com.gmoon.cacheinvalidation.test.Profiles;

/**
 * 브로커는 이 모듈만 쓰므로 공용 테스트 모듈의 컨테이너 묶음에 넣지 않는다.
 */
@Profile(Profiles.TESTCONTAINERS)
@TestConfiguration(proxyBeanMethods = false)
public class RabbitContainerConfig {

	private static final DockerImageName RABBITMQ_IMAGE = DockerImageName.parse("rabbitmq:4-management-alpine");
	private static final boolean REUSABLE = System.getenv("CI") == null;

	private static final RabbitMQContainer RABBITMQ = new RabbitMQContainer(RABBITMQ_IMAGE)
		 .withReuse(REUSABLE);

	static {
		RABBITMQ.start();
	}

	@Bean
	@ServiceConnection
	public RabbitMQContainer rabbitContainer() {
		return RABBITMQ;
	}
}
