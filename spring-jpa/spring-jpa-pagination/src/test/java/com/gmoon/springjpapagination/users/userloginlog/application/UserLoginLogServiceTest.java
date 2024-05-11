package com.gmoon.springjpapagination.users.userloginlog.application;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.springjpapagination.global.domain.CursorPagination;
import com.gmoon.springjpapagination.users.userloginlog.dto.UserLoginLogListVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
@SpringBootTest
class UserLoginLogServiceTest {
	@Autowired
	UserLoginLogService service;

	@Test
	void testGetUserLoginLogListVO() {
		// given
		UserLoginLogListVO listVO = new UserLoginLogListVO();
		listVO.setCursor("1900-02-02 12:34:43000000000000000000000000000000000000000000000lul06");

		// when
		listVO = service.getUserLoginLogListVO(listVO);

		// then
		log.info("조회된 마지막 행의 커서 데이터: {}", listVO.getCursor());
		log.info("다음 페이지 존재 여부: {}", listVO.isHasNextPage());
		log.info("요청 커서에 대한 결과: {}", listVO.getList());
		assertThat(listVO)
			.hasFieldOrPropertyWithValue("cursor", CursorPagination.EMPTY_CURSOR)
			.hasFieldOrPropertyWithValue("hasNextPage", false);
	}
}
