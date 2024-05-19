package com.gmoon.resourceserver.cors;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import com.gmoon.resourceserver.test.BaseJpaTest;

class CorsHttpMethodRepositoryTest extends BaseJpaTest {
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
		List<String> allowedMethods = repository.findAllByEnabled();

		// then
		assertThat(allowedMethods)
			 .containsOnly(HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.DELETE.name(),
				  HttpMethod.PUT.name());
	}
}
