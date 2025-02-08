package com.gmoon.springreactive.config;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
class WebClientPropertiesTest {

	@Autowired
	private WebClientProperties properties;

	@Test
	void test() {
		log.info("{}", properties);
		log.info("max byte count         :{}", properties.getMaxInMemorySize());
		log.info("connect timeout        :{}", properties.getConnectTimeout());
		log.info("response timeout       :{}", properties.getResponseTimeout());
		log.info("max idle time          :{}", properties.getMaxIdleTime());
		log.info("max life time          :{}", properties.getMaxLifeTime());
		log.info("read timeout           :{}", properties.getReadTimeout());
		log.info("write timeout          :{}", properties.getWriteTimeout());

		assertThat(properties.getMaxInMemorySize()).isNotNull();
		assertThat(properties.getConnectTimeout()).isGreaterThan(Duration.ZERO);
		assertThat(properties.getResponseTimeout()).isGreaterThan(Duration.ZERO);

		assertThat(properties.getMaxIdleTime()).isGreaterThan(Duration.ZERO);
		assertThat(properties.getMaxLifeTime()).isGreaterThan(Duration.ZERO);
		assertThat(properties.getReadTimeout()).isGreaterThan(Duration.ZERO);
		assertThat(properties.getWriteTimeout()).isGreaterThan(Duration.ZERO);
	}
}
