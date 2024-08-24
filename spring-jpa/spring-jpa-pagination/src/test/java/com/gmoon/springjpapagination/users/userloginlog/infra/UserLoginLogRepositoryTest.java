package com.gmoon.springjpapagination.users.userloginlog.infra;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import com.gmoon.springjpapagination.global.config.JpaConfig;
import com.gmoon.springjpapagination.users.userloginlog.domain.UserLoginLogRepository;
import com.gmoon.springjpapagination.users.userloginlog.dto.UserLoginLogListVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@DataJpaTest(
	 includeFilters = @ComponentScan.Filter(
		  type = FilterType.ASSIGNABLE_TYPE,
		  value = {
			   JpaConfig.class,
			   UserLoginLogRepositoryAdapter.class
		  }
	 )
)
class UserLoginLogRepositoryTest {

	@Autowired
	private UserLoginLogRepository repository;

	@Test
	void cursor() {
		UserLoginLogListVO listVO = new UserLoginLogListVO();

		int totalSize = 0;
		do {
			listVO = repository.getUserLoginLogListVO(listVO);
			totalSize += listVO.getList().size();
			log.info("listVO: {}", totalSize);
		} while (listVO.isHasNextPage());

		assertThat(totalSize)
			 .isEqualTo(repository.findAll().size());
	}
}
