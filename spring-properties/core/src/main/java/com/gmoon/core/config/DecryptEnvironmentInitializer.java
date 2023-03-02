package com.gmoon.core.config;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class DecryptEnvironmentInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	//https://stackoverflow.com/questions/55996663/spring-properties-decryption
	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		decryptEnvironmentProperty(applicationContext);
	}

	private void decryptEnvironmentProperty(ConfigurableApplicationContext applicationContext) {
		ConfigurableEnvironment environment = applicationContext.getEnvironment();
		ConfigurableConversionService conversionService = environment.getConversionService();
		conversionService.addConverter(new JasyptDecryptConvertor(environment));
	}

	@Slf4j
	static class JasyptDecryptConvertor implements Converter<String, String> {

		private final StringEncryptor stringEncryptor;

		JasyptDecryptConvertor(Environment env) {
			String password = env.getProperty("jasypt.encryptor.password");
			this.stringEncryptor = createStringEncryptor(password);
		}

		private StringEncryptor createStringEncryptor(String password) {
			SimpleStringPBEConfig config = new SimpleStringPBEConfig();
			config.setPassword(password);
			config.setAlgorithm("PBEWithMD5AndDES");
			config.setKeyObtentionIterations("1000");
			config.setPoolSize("1");
			config.setProviderName("SunJCE");
			config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
			config.setStringOutputType("base64");

			PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
			encryptor.setConfig(config);
			return encryptor;
		}

		@Override
		public String convert(String source) {
			return decrypt(source);
		}

		private String decrypt(String source) {
			try {
				return stringEncryptor.decrypt(source);
			} catch (Exception ex) {
				log.warn("property decrypt error... source: {}", source);
				return source;
			}

		}
	}
}
