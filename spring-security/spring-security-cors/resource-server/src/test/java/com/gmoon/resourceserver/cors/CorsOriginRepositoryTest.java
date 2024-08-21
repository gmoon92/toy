package com.gmoon.resourceserver.cors;

import static org.assertj.core.api.Assertions.assertThat;

import com.gmoon.resourceserver.config.JpaConfig;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Slf4j
@Import(JpaConfig.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CorsOriginRepositoryTest {

	@Autowired
	private CorsOriginRepository repository;

	@Test
	void getAllHost() {
		List<String> hosts = repository.getAllHost();

		assertThat(hosts).contains("localhost", "127.0.0.1", "gmoon92.github.io");
	}
}
