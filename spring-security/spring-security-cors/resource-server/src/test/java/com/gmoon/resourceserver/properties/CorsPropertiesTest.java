package com.gmoon.resourceserver.properties;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CorsPropertiesTest {
	@Autowired
	CorsProperties properties;

	@Test
	@DisplayName("cors.yml 에 정의된 프로퍼티 속성 값을 검증한다.")
	void testVerifyCorsProperties() {
		// given

		// when
		boolean enabled = properties.isEnabled();

		// then
		assertThat(enabled).isTrue();
	}
}
