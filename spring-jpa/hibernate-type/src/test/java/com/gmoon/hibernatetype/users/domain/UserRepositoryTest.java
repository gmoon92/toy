package com.gmoon.hibernatetype.users.domain;

import com.gmoon.hibernatetype.test.AbstractRepositoryTest;
import com.gmoon.hibernatetype.users.infra.UserRepositoryAdapter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@Slf4j
@Import(UserRepositoryAdapter.class)
class UserRepositoryTest extends AbstractRepositoryTest {

	@Autowired
	private UserRepository repository;

	@Test
	void findById() {
		assertThatCode(() -> repository.findById("1"))
			 .doesNotThrowAnyException();
	}

	@Test
	@Rollback(value = false)
	void save() {
		String mail = "gmoon92@gmail.com";

		User savedUser = repository.save(User.builder()
			 .email(mail)
			 .encEmail(mail)
			 .build());

		flushAndClear();

		assertThat(repository.findById(savedUser.getId()))
			 .map(User::getEmail)
			 .get()
			 .isEqualTo(mail);
	}

	@Test
	void findAllByEncEmail() {
		String mail = "test@gmail.com";
		repository.save(User.builder()
			 .email(mail)
			 .encEmail(mail)
			 .build());

		flushAndClear();

		assertThat(repository.findAllByEncEmail(mail))
			 .map(User::getEncEmail)
			 .containsExactly("test@gmail.com");
	}
}
