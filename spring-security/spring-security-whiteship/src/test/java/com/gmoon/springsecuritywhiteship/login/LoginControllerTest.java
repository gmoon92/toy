package com.gmoon.springsecuritywhiteship.login;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.gmoon.springsecuritywhiteship.account.Account;
import com.gmoon.springsecuritywhiteship.account.AccountRepository;
import com.gmoon.springsecuritywhiteship.account.AccountService;
import com.gmoon.springsecuritywhiteship.account.UserController;
import com.gmoon.springsecuritywhiteship.board.BoardRepository;
import com.gmoon.springsecuritywhiteship.sample.SampleService;

@WebMvcTest(value = {UserController.class})
class LoginControllerTest {
	@Autowired WebApplicationContext context;
	MockMvc mockMvc;

	@MockBean AccountService accountService;
	@MockBean AccountRepository accountRepository;
	@MockBean SampleService sampleService;
	@MockBean BoardRepository boardRepository;

	String username;
	String password;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context)
			.alwaysDo(print())
			.apply(springSecurity())
			.build();

		username = RandomStringUtils.randomAlphanumeric(4);
		password = "123";

		PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
		Account account = Account.newUser(username, passwordEncoder.encode(password));
		when(accountService.createNew(any())).thenReturn(account);
		when(accountService.loadUserByUsername(any())).thenReturn(account);
	}

	@Test
	void index_anonymous() throws Exception {
		// when
		ResultActions result = mockMvc.perform(get("/"));

		// then
		result.andExpect(status().isOk());
	}

	@Test
	void index_user() throws Exception {
		// when
		ResultActions result = mockMvc.perform(get("/")
			.with(user("gmoon") // mock already login user
				.roles("USER")));

		// then
		result.andExpect(status().isOk());
	}

	@Test
	void index_admin() throws Exception {
		// when
		ResultActions result = mockMvc.perform(get("/")
			.with(user("admin") /* mock already login user*/
				.roles("ADMIN")));

		// then
		result.andExpect(status().isOk());
	}

	@Test
	void admin_user() throws Exception {
		// when
		ResultActions result = mockMvc.perform(get("/admin")
			.with(user("gmoon")
				.roles("USER")));

		// then
		// result.andExpect(status().isForbidden());
		result.andExpect(status().is3xxRedirection());
	}

	@Test
	void admin_admin() throws Exception {
		// when
		ResultActions result = mockMvc.perform(get("/admin")
			.with(user("admin")
				.roles("ADMIN")));

		// then
		result.andExpect(status().isOk());
	}

	@Test
	@WithAnonymousUser
	void index_anonymous_with_test_annotation() throws Exception {
		// when
		ResultActions result = mockMvc.perform(get("/"));

		// then
		result.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "gmoon", roles = {"USER"})
	void index_user_with_test_annotation() throws Exception {
		// when
		ResultActions result = mockMvc.perform(get("/"));

		// then
		result.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "admin", roles = {"ADMIN"})
	void admin_admin_with_test_annotation() throws Exception {
		// when
		ResultActions result = mockMvc.perform(get("/admin"));

		// then
		result.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "user", roles = {"USER"})
	void admin_user_with_test_annotation() throws Exception {
		// when
		ResultActions result = mockMvc.perform(get("/admin"));

		// then
		// result.andExpect(status().isForbidden());
		result.andExpect(status().is3xxRedirection());
	}

	@Test
	@DisplayName("Security formLogin test")
	void form_login() throws Exception {
		// when
		ResultActions result = mockMvc.perform(formLogin()
			.userParameter("id")
			.user(username)
			.password(password));

		// then
		result.andExpect(authenticated());
	}

	@Test
	@DisplayName("Security formLogin test")
	void form_login_error() throws Exception {
		// when
		ResultActions result = mockMvc.perform(formLogin()
			.userParameter("id")
			.user(username)
			.password(password + "test"));

		// then
		result.andExpect(unauthenticated());
	}
}
