package com.gmoon.resourceclient.user;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class UserControllerTest {

	@Autowired
	WebApplicationContext context;

	MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context)
			 .apply(springSecurity())
			 .alwaysDo(print())
			 .build();
	}

	@Test
	@Disabled("Resource Server 어플리케이션 실행 후 테스트")
	void testFormLogin() throws Exception {
		// given
		String username = "admin";
		String password = "123";

		// when
		ResultActions result = mockMvc.perform(formLogin()
			 .user(username)
			 .password(password));

		// then
		result.andExpect(authenticated());
	}
}
