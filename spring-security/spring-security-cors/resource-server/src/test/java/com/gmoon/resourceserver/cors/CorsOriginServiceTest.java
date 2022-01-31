package com.gmoon.resourceserver.cors;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CorsOriginServiceTest {
	@Autowired CorsOriginService service;

	@Test
	@DisplayName("CORS 필터에서 체크할 origin 정규식을 반환한다.")
	void testGetAllowedOriginPatterns() {
		// when
		List<String> patterns = service.getAllowedOriginPatterns();

		// then
		assertThat(patterns)
			.contains("**://localhost:**", "**://localhost");
	}
}
