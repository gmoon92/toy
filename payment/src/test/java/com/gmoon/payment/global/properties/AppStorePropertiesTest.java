package com.gmoon.payment.global.properties;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.apple.itunes.storekit.model.Environment;
import com.gmoon.payment.test.UnitTestCase;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@UnitTestCase
class AppStorePropertiesTest {

	@Autowired
	private AppStoreProperties properties;

	@Test
	void test() {
		log.info("{}", properties);

		assertThat(properties.getEnvironment()).isEqualTo(Environment.SANDBOX);
		assertThat(properties.privateKey().id()).isNotNull();
		assertThat(properties.privateKey().filePath()).isNotNull();
	}
}
