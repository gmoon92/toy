package com.gmoon.springjpapagination.users.user.application;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.gmoon.springjpapagination.users.user.dto.UserContentListListVO;

@SpringBootTest
class UserServiceTest {

	@Autowired
	private UserService service;

	@Test
	void getUserContentListVO() {
		UserContentListListVO listVO = new UserContentListListVO();

		listVO = service.getUserContentListVO(listVO);

		assertThat(listVO.getData()).isNotEmpty();
	}
}
