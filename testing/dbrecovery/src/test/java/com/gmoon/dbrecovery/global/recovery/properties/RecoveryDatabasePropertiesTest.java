package com.gmoon.dbrecovery.global.recovery.properties;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class RecoveryDatabasePropertiesTest {

	@Autowired
	private RecoveryDatabaseProperties properties;

	@Test
	void test() {
		log.info("properties: {}", properties);
		assertThat(properties.getSchema()).isNotBlank();
		assertThat(properties.getRecoverySchema()).isNotBlank();
	}
}
