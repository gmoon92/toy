package com.gmoon.core.utils;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class SystemPropertiesTest {

	@Autowired
	private SystemProperties systemProperties;

	@DisplayName("core application.properties 재정의(#---)")
	@Test
	void overridingCorePropertyValue() {
		String moduleName = systemProperties.getModuleName();

		log.info("moduleName: {}", moduleName);
		assertThat(moduleName).isEqualTo("web");
	}

	@Test
	void decrypt() {
		assertThat(systemProperties.getEmailPassword())
			 .isEqualTo("123");
	}
}
