package com.gmoon.custom.service;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.gmoon.core.service.UserService;

@SpringBootTest
class UserServiceCustomTest {

	@Autowired
	private UserService userService;

	@Test
	void getUsername() {
		String username = userService.getUsername();

		assertThat(username)
			.isEqualTo("web admin");
	}
}
