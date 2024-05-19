package com.gmoon.springsecuritywhiteship.account;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class PasswordEncoderTest {

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void encode() {
		String rawPassword = "123";
		String password = passwordEncoder.encode(rawPassword);
		String password2 = passwordEncoder.encode(rawPassword);

		log.info("password: {}", password);
		log.info("password2: {}", password2);

		assertThat(passwordEncoder.matches(rawPassword, password))
			 .isTrue();
		assertThat(passwordEncoder.matches(rawPassword, password2))
			 .isTrue();
	}
}
