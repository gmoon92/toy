package com.gmoon.hibernatetype.users.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

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
	void save() {
		repository.save(User.builder()
			.email("gmoon92@gmail.com")
			.build());
	}
}
