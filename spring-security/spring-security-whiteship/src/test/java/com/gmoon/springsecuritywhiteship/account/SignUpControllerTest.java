package com.gmoon.springsecuritywhiteship.account;

import static org.hamcrest.core.StringContains.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
class SignUpControllerTest {
	private static final String URL_OF_SIGNUP = "/signup";

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("회원가입 폼에 csrf 태그가 포함되는지")
	void form() throws Exception {
		// when
		ResultActions result = mockMvc.perform(get(URL_OF_SIGNUP));

		// then
		result.andExpect(status().isOk())
			.andExpect(content().string(containsString("_csrf")));
	}

	@Test
	@DisplayName("회원 가입 csrf 토큰 미포함시 forbidden 403 에러 확인")
	void save_403_error_when_non_csrf_token() throws Exception {
		// when
		ResultActions result = mockMvc.perform(post(URL_OF_SIGNUP)
				.param("username", RandomStringUtils.randomAlphanumeric(8))
				.param("password", RandomStringUtils.randomAlphanumeric(8)))
			.andDo(print());

		// then
		result.andExpect(status().is3xxRedirection());
	}

	@Test
	@DisplayName("회원 가입 csrf 토큰을 포함한다.")
	void save() throws Exception {
		// when
		ResultActions result = mockMvc.perform(post(URL_OF_SIGNUP)
				.param("username", RandomStringUtils.randomAlphanumeric(8))
				.param("password", RandomStringUtils.randomAlphanumeric(8))
				.with(csrf()))
			.andDo(print());

		// then
		result.andExpect(status().is3xxRedirection());
	}
}
