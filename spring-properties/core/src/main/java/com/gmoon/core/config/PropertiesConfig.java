package com.gmoon.core.config;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.PBEConfig;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

import lombok.RequiredArgsConstructor;

// https://www.baeldung.com/spring-boot-jasypt
// @Import(EnableEncryptablePropertiesConfiguration.class)
@Configuration
@RequiredArgsConstructor
public class PropertiesConfig {

	private final Environment env;

	/**
	 * @see org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration
	 * */
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
		configurer.setIgnoreResourceNotFound(true);
		configurer.setIgnoreUnresolvablePlaceholders(true);
		return configurer;
	}

	@Bean
	public PropertiesConfiguration runtimeProperties() {
		String runtimePropertiesPath = env.getProperty("runtime.properties.path");
		try {
			PropertiesConfiguration configuration = new PropertiesConfiguration(runtimePropertiesPath);
			configuration.setReloadingStrategy(new FileChangedReloadingStrategy());
			return configuration;
		} catch (ConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see com.ulisesbocchio.jasyptspringboot.configuration.EncryptablePropertyResolverConfiguration
	 * @see com.ulisesbocchio.jasyptspringboot.configuration.EnableEncryptablePropertiesConfiguration
	 * @see com.ulisesbocchio.jasyptspringboot.EncryptablePropertySourceConverter
	 * @see com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties
	 * */
	@Primary
	@Bean("customBeanName")
	public StringEncryptor stringEncryptor() {
		PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
		encryptor.setConfig(getPBEConfig());
		return encryptor;
	}

	private PBEConfig getPBEConfig() {
		SimpleStringPBEConfig config = new SimpleStringPBEConfig();
		config.setPassword("jasyptPassword");
		config.setAlgorithm("PBEWithMD5AndDES");
		config.setKeyObtentionIterations("1000");
		config.setPoolSize("1");
		config.setProviderName("SunJCE");
		config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
		config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
		config.setStringOutputType("base64");
		return config;
	}
}
