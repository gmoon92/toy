package com.gmoon.springjpapagination.users.user.infra;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import com.gmoon.springjpapagination.global.domain.BasePageable;
import com.gmoon.springjpapagination.global.domain.RepositoryTest;
import com.gmoon.springjpapagination.users.user.domain.UserGroupRepository;
import com.gmoon.springjpapagination.users.user.dto.UserGroupListVO;

@Import(UserGroupRepositoryAdapter.class)
@RepositoryTest
class UserGroupRepositoryTest {

	@Autowired
	private UserGroupRepository repository;

	@Test
	void findAll() {
		BasePageable pageable = new UserGroupListVO();

		assertThatCode(() -> repository.findAll(null, null, pageable))
			 .doesNotThrowAnyException();
	}

	@Test
	void countBy() {
		assertThat(repository.countBy(null, null))
			 .isGreaterThanOrEqualTo(0);
	}
}
