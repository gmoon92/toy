package com.gmoon.cacheinvalidation.test.measure;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import jakarta.persistence.EntityManagerFactory;

/**
 * 검증에만 쓰이는 측정 도구를 등록한다.
 * 제품 코드가 의존하지 않으므로 코어가 아니라 테스트 모듈이 소유한다.
 */
@TestConfiguration(proxyBeanMethods = false)
public class MeasureConfig {

	@Bean
	public DatabaseQueryCounter databaseQueryCounter(EntityManagerFactory entityManagerFactory) {
		return new DatabaseQueryCounter(entityManagerFactory);
	}

}
