package com.gmoon.springjpapagination.users.user.application;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.gmoon.springjpapagination.users.user.dto.UserContentListVO;
import com.gmoon.springjpapagination.users.user.dto.UserContentVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
class UserServiceTest {

	@Autowired
	private UserService service;

	@Test
	void getUserContentListVO() {
		UserContentListVO listVO = new UserContentListVO();
		listVO.setPage(1);
		listVO.setPageSize(10);

		listVO = service.getUserContentListVO(listVO);

		assertThat(listVO.getContent()).isNotEmpty();
		assertThat(listVO.getContent().size()).isLessThanOrEqualTo(listVO.getPageSize());
		assertThat(listVO.getContent())
			 .filteredOn(vo -> vo.getType().equals(UserContentVO.Type.USER_GROUP))
			 .size()
			 .isEqualTo(3);
		assertThat(listVO.getContent())
			 .filteredOn(vo -> vo.getType().equals(UserContentVO.Type.USER))
			 .size()
			 .isLessThanOrEqualTo(7);
	}
}
