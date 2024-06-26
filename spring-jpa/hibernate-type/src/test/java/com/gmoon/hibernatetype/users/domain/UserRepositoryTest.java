package com.gmoon.hibernatetype.users.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;

import com.gmoon.hibernatetype.global.JpaConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Import(JpaConfig.class)
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

		User merged = repository.save(User.builder()
			 .email(mail)
			 .encEmail(mail)
			 .build());

		log.info("enc: {}", merged.getEncEmail());
		log.info("enc: {}", merged.getEncEmail());
		assertThat(repository.findAllByEncEmail(mail))
			 .isNotEmpty();
	}
}
