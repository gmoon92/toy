package com.gmoon.springjpapagination.users.user.infra;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import com.gmoon.springjpapagination.global.config.JpaConfig;
import com.gmoon.springjpapagination.global.domain.BasePageable;
import com.gmoon.springjpapagination.users.user.domain.UserGroupRepository;
import com.gmoon.springjpapagination.users.user.dto.UserGroupListVO;

@DataJpaTest(
	 includeFilters = @ComponentScan.Filter(
		  type = FilterType.ASSIGNABLE_TYPE,
		  value = {
			   JpaConfig.class,
			   UserGroupRepositoryAdapter.class
		  }
	 )
)
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
