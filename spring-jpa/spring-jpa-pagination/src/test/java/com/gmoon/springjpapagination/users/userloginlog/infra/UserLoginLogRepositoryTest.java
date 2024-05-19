package com.gmoon.springjpapagination.users.userloginlog.infra;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import com.gmoon.springjpapagination.global.config.JpaConfig;
import com.gmoon.springjpapagination.global.domain.CursorPagination;
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
	void getUserLoginLogListVO() {
		UserLoginLogListVO listVO = new UserLoginLogListVO();

		listVO = repository.getUserLoginLogListVO(listVO);

		log.info("조회된 마지막 행의 커서 데이터: {}", listVO.getCursor());
		log.info("다음 페이지 존재 여부: {}", listVO.isHasNextPage());
		log.info("요청 커서에 대한 결과: {}", listVO.getList());
		assertThat(listVO.getCursor()).isNotEqualTo(CursorPagination.EMPTY_CURSOR);
		assertThat(listVO.isHasNextPage()).isTrue();
	}
}
