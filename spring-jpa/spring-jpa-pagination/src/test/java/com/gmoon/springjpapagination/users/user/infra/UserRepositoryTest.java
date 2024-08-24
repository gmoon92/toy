package com.gmoon.springjpapagination.users.user.infra;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import com.gmoon.springjpapagination.global.domain.RepositoryTest;
import com.gmoon.springjpapagination.users.user.domain.UserRepository;
import com.gmoon.springjpapagination.users.user.dto.UserContentListVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Import(UserRepositoryAdapter.class)
@RepositoryTest
class UserRepositoryTest {

	@Autowired
	private UserRepository repository;

	@Test
	void findAll() {
		String groupId = "";
		String keyword = "";

		assertThatCode(() -> repository.findAll(
			 groupId,
			 keyword,
			 new UserContentListVO()
		)).doesNotThrowAnyException();
	}

	@Test
	void countBy() {
		String groupId = "";
		String keyword = "";

		assertThatCode(() -> repository.countBy(
			 groupId,
			 keyword
		)).doesNotThrowAnyException();
	}

}
