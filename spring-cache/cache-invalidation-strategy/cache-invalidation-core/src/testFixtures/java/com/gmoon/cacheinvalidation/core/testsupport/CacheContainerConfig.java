package com.gmoon.cacheinvalidation.core.testsupport;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;

@Profile(CacheTestProfiles.TESTCONTAINERS)
@TestConfiguration(proxyBeanMethods = false)
public class CacheContainerConfig {

	@Bean
	@ServiceConnection
	public MySQLContainer<?> mysqlContainer() {
		return CacheTestContainers.mysql();
	}

	@Bean
	@ServiceConnection(name = "redis")
	public GenericContainer<?> redisContainer() {
		return CacheTestContainers.redis();
	}
}
