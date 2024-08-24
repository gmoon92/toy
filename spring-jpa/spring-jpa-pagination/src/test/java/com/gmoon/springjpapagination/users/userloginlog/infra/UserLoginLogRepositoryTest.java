package com.gmoon.springjpapagination.users.userloginlog.infra;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import com.gmoon.springjpapagination.global.domain.RepositoryTest;
import com.gmoon.springjpapagination.users.userloginlog.domain.UserLoginLogRepository;
import com.gmoon.springjpapagination.users.userloginlog.dto.UserLoginLogListVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Import(UserLoginLogRepositoryAdapter.class)
@RepositoryTest
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
