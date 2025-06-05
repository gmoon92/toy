package com.gmoon.web.config;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.gmoon.core.service.TeamService;
import com.gmoon.core.service.UserService;
import com.gmoon.core.service.UserServiceImpl;
import com.gmoon.custom.service.UserServiceCustom;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
class OverrideComponentExcludeFilterTest {

	@Autowired
	private UserService userService;

	@Autowired
	private UserServiceImpl userServiceImpl;

	@Autowired
	private UserServiceCustom userServiceCustom;

	@Autowired
	private TeamService teamService;

	@DisplayName("빈 재정의")
	@Test
	void overrideCustomBean() {
		assertThat(userService).isInstanceOf(UserServiceCustom.class);
		assertThat(userServiceImpl).isInstanceOf(UserServiceCustom.class);
		assertThat(userServiceCustom).isInstanceOf(UserServiceCustom.class);

		assertThat(teamService).isInstanceOf(TeamService.class);
	}
}
