package com.gmoon.springjpapagination.users.user.infra;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import com.gmoon.springjpapagination.global.config.JpaConfig;
import com.gmoon.springjpapagination.users.user.domain.UserRepository;
import com.gmoon.springjpapagination.users.user.dto.UserContentListVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@DataJpaTest(
	includeFilters = @ComponentScan.Filter(
		type = FilterType.ASSIGNABLE_TYPE,
		value = {
			JpaConfig.class,
			UserRepositoryAdapter.class
		}
	)
)
class UserRepositoryTest {

	@Autowired
	private UserRepository repository;

	@Test
	void getUserContents() {
		String groupId = "";
		String keyword = "";

		assertThatCode(() -> repository.getUserContents(
			groupId,
			keyword,
			new UserContentListVO()
		)).doesNotThrowAnyException();
	}

	@Test
	void getUserContentTotalCount() {
		String groupId = "";
		String keyword = "";

		assertThatCode(() -> repository.getUserContentTotalCount(
			groupId,
			keyword
		)).doesNotThrowAnyException();
	}

}
