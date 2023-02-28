package com.gmoon.web.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.gmoon.core.service.UserService;

@SpringBootTest
class UserServiceCustomTest {

	@Autowired
	private UserService userService;

	@Test
	void test() {
		String username = userService.getUsername();
		System.out.println("username: " + username);
	}
}
