package com.gmoon.resourceserver.cors;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.gmoon.resourceserver.config.JpaConfig;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
class CorsOriginRepositoryTest {
	@Autowired CorsOriginRepository repository;

	@Test
	void testGetAllHost() {
		// when
		List<String> hosts = repository.getAllHost();

		// then
		assertThat(hosts)
			.contains("localhost", "127.0.0.1", "gmoon92.github.io");
	}
}
