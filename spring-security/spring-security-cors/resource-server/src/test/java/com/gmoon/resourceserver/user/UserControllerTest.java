package com.gmoon.resourceserver.user;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class UserControllerTest {
	@Autowired WebApplicationContext context;

	MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context)
			.apply(springSecurity())
			.alwaysDo(print())
			.build();
	}

	@Test
	void testLogin() throws Exception {
		// given
		String username = "admin";
		String password = "123";

		// when
		ResultActions result = postLogin(username, password);

		// then
		result.andExpect(status().isOk());
		result.andExpect(header().exists(HttpHeaders.AUTHORIZATION));
	}

	@Test
	@DisplayName("사용자 인증 실패")
	void testLogin_fail() throws Exception {
		// given
		String username = "admin";
		String password = UUID.randomUUID().toString();

		// when
		ResultActions result = postLogin(username, password);

		// then
		result.andExpect(status().isUnauthorized());
	}

	private ResultActions postLogin(String username, String password) throws Exception {
		return mockMvc.perform(post("/login")
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.characterEncoding(StandardCharsets.UTF_8)
			.param("username", username)
			.param("password", password));
	}
}
