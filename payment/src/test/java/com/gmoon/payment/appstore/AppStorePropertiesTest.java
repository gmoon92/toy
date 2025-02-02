package com.gmoon.payment.appstore;

import com.gmoon.payment.test.UnitTestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@UnitTestCase
class AppStorePropertiesTest {

	@Autowired
	private AppStoreProperties properties;

	@Test
	void test() {
		log.info("{}", properties);

		assertThat(properties.environment()).isEqualTo(AppStoreEnvironment.SANDBOX);
		assertThat(properties.privateKey().id()).isNotNull();
		assertThat(properties.privateKey().filePath()).isNotNull();
	}
}
