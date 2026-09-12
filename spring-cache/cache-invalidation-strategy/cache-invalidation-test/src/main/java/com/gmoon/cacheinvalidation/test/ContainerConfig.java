package com.gmoon.cacheinvalidation.test;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;

@Profile(Profiles.TESTCONTAINERS)
@TestConfiguration(proxyBeanMethods = false)
public class ContainerConfig {

	@Bean
	@ServiceConnection
	public MySQLContainer<?> mysqlContainer() {
		return Containers.mysql();
	}

	@Bean
	@ServiceConnection(name = "redis")
	public GenericContainer<?> redisContainer() {
		return Containers.redis();
	}
}
