package com.gmoon.timesorteduniqueidentifier.users.user.adapter.out.persistence;

import com.gmoon.timesorteduniqueidentifier.global.base.annotation.PersistenceAdapterTest;
import com.gmoon.timesorteduniqueidentifier.users.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@PersistenceAdapterTest(value = UserRepositoryAdapter.class)
class UserRepositoryAdapterTest {

	@Autowired
	private UserRepositoryAdapter adapter;

	@Test
	void findAll() {
		assertThat(adapter.findAll()).isNotEmpty();
	}

	@Test
	void get() {
		assertThatCode(() -> adapter.findAll()).doesNotThrowAnyException();
	}

	@Test
	void save() {
		assertThatCode(() -> adapter.save(new User("guest", "password")))
			 .doesNotThrowAnyException();
	}
}
