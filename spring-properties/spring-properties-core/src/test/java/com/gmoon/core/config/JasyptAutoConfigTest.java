package com.gmoon.core.config;

import static org.assertj.core.api.Assertions.*;

import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

/**
 * @see com.ulisesbocchio.jasyptspringbootstarter.JasyptSpringBootAutoConfiguration
 * @see com.ulisesbocchio.jasyptspringboot.resolver.DefaultPropertyResolver
 * */
@SpringBootTest
public class JasyptAutoConfigTest {

	@Autowired
	private Environment env;

	@Autowired
	private StringEncryptor stringEncryptor;

	@Value("${enc-message}")
	private String encMessage;

	@Test
	void encrypt() {
		String decrypted = env.getProperty("dec-message");
		String encrypted = stringEncryptor.encrypt(decrypted);

		assertThat(stringEncryptor.decrypt(encrypted))
			 .isEqualTo(env.getProperty("enc-message"))
			 .isEqualTo(decrypted)
			 .isEqualTo(encMessage);
	}
}
