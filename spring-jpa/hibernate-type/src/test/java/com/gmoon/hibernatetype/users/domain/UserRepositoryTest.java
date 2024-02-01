package com.gmoon.hibernatetype.users.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

	@Autowired
	private UserRepository repository;

	@Test
	void find() {
		repository.findById("user0");
	}

	@Test
	@Rollback(value = false)
	void save() {
		String mail = "gmoon92@gmail.com";

		repository.save(User.builder()
			.email(mail)
			.encEmail(mail)
			.build());
	}

	@Test
	void findAllByEncEmail() {
		String mail = "test@gmail.com";

		repository.save(User.builder()
			.email(mail)
			.encEmail(mail)
			.build());

		assertThat(repository.findAllByEncEmail(mail))
			.isNotEmpty();
	}
}
