package com.gmoon.resourceclient.user;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

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
