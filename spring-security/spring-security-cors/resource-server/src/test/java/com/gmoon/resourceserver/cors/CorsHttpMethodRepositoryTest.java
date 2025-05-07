package com.gmoon.resourceserver.cors;

import com.gmoon.resourceserver.test.AbstractJpaRepositoryTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CorsHttpMethodRepositoryTest extends AbstractJpaRepositoryTest {
	@Autowired
	CorsHttpMethodRepository repository;

	@Test
	void testSave() {
		// given
		CorsHttpMethod method = CorsHttpMethod.create(HttpMethod.POST);

		// when
		CorsHttpMethod savedCorsHttpMethod = repository.save(method);

		// then
		assertThat(savedCorsHttpMethod.isEnabled()).isFalse();
	}

	@Test
	@DisplayName("활성화된 HTTP Method 조회")
	void testFindAllByEnabled() {
		// when
		List<HttpMethod> allowedMethods = repository.findAllByEnabled()
			 .stream()
			 .map(HttpMethod::valueOf)
			 .toList();

		// then
		assertThat(allowedMethods)
			 .containsOnly(
				  HttpMethod.GET,
				  HttpMethod.POST,
				  HttpMethod.DELETE,
				  HttpMethod.PUT
			 );
	}
}
