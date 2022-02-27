package com.gmoon.resourceserver.cors;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;

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

	@Test
	@DisplayName("모든 CORS 관련 Origin 을 조회한다.")
	void testGetAll() {
		// when
		List<CorsOrigin> corsOrigin = service.getAll();

		// then
		assertThat(corsOrigin).isNotEmpty();
	}

	@Test
	@DisplayName("CORS Origin 을 저장한다.")
	void testSave() {
		// given
		Origin origin = Origin.builder()
			.host(RandomStringUtils.randomAlphanumeric(10))
			.build();
		CorsOrigin corsOrigin = CorsOrigin.create(origin);

		// when
		CorsOrigin actual = service.save(corsOrigin);

		// then
		assertThat(actual).isNotNull();
	}

	@Test
	@DisplayName("저장된 CORS 데이터가 있다면 삭제한다.")
	void testDelete() {
		// given
		Long id = 1L;

		// when then
		service.delete(id);
	}

	@Test
	@DisplayName("Access-Control-Allow-Methods 에 등록할 HTTP Method 조회")
	void testGetAllowedHttpMethods() {
		// when
		List<String> methods = service.getAllowedHttpMethods();

		// then
		assertThat(methods)
			.containsOnly(httpName(HttpMethod.GET), httpName(HttpMethod.POST), httpName(HttpMethod.DELETE),
				httpName(HttpMethod.PUT));
	}

	private String httpName(HttpMethod httpMethod) {
		return httpMethod.name();
	}
}
