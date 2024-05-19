package com.gmoon.springsecuritycsrfaspect.user;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.gmoon.springsecuritycsrfaspect.csrf.CsrfTokenRepository;
import com.gmoon.springsecuritycsrfaspect.csrf.token.BaseCsrfToken;
import com.gmoon.springsecuritycsrfaspect.csrf.token.HttpSessionCsrfToken;

@SpringBootTest
class UserControllerTest {

	@Autowired
	HttpServletRequest request;
	@Autowired
	CsrfTokenRepository repository;
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
	@DisplayName("사용자 인증 후 Http Session 에 CSRF 토큰이 생성된다.")
	void login() throws Exception {
		// given
		String attributeName = repository.getSessionAttributeName();

		// when
		ResultActions result = mockMvc.perform(post("/login")
			 .accept(MediaType.APPLICATION_FORM_URLENCODED)
			 .contentType(MediaType.APPLICATION_FORM_URLENCODED)
			 .param("username", "admin")
			 .param("password", "123"));

		// then
		result.andExpect(request().sessionAttribute(attributeName, is(notNullValue(HttpSessionCsrfToken.class))))
			 .andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("CSRF 토큰을 체크한다.")
	void delete() throws Exception {
		// given
		repository.saveTokenOnHttpSession(request);
		BaseCsrfToken token = repository.getToken(request);

		// when
		String attributeName = repository.getSessionAttributeName();
		String headerName = token.getHeaderName();
		String value = token.getValue();
		ResultActions result = mockMvc.perform(get("/user/delete")
			 .sessionAttr(attributeName, token)
			 .header(headerName, value));

		// then
		result.andExpect(request().sessionAttribute(attributeName, is(notNullValue(HttpSessionCsrfToken.class))))
			 .andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("CSRF 토큰이 없을 경우 예외가 발생한다.")
	void delete_non_csrf_token_error() throws Exception {
		// given
		repository.saveTokenOnHttpSession(request);
		BaseCsrfToken token = repository.getToken(request);

		// when
		String attributeName = repository.getSessionAttributeName();
		ResultActions result = mockMvc.perform(get("/user/delete")
			 .sessionAttr(attributeName, token));

		// then
		result.andExpect(request().sessionAttribute(attributeName, is(notNullValue(HttpSessionCsrfToken.class))))
			 .andExpect(status().is3xxRedirection());
	}
}
