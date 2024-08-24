package com.gmoon.resourceserver.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void login() throws Exception {
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
	void loginFail() throws Exception {
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
