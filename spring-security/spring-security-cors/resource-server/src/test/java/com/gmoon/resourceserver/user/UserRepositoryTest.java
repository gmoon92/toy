package com.gmoon.resourceserver.user;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.gmoon.resourceserver.config.JpaConfig;

@DataJpaTest
@Import(JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {
	@Autowired
	UserRepository repository;

	@Test
	void testFindByUsername() {
		// given
		String username = "admin";

		// when
		User actual = repository.findByUsername(username);

		// then
		assertThat(actual)
			 .hasFieldOrPropertyWithValue("username", "admin")
			 .hasFieldOrPropertyWithValue("role", Role.ADMIN);
	}
}
