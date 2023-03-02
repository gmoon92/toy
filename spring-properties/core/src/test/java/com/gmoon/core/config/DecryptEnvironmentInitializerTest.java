package com.gmoon.core.config;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import com.gmoon.core.utils.SystemProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
class DecryptEnvironmentInitializerTest {

	@Autowired
	private Environment env;

	@Autowired
	private SystemProperties systemProperties;

	@Value("${email.password}")
	private String emailPassword;

	@Test
	void decrypt() {
		String property = env.getProperty("email.password");

		log.info("property: {}", property);

		assertThat(property)
			.isEqualTo(emailPassword)
			.isEqualTo(systemProperties.getEmailPassword())
			.isEqualTo("123");
	}
}
