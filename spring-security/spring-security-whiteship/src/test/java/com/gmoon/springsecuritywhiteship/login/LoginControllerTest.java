package com.gmoon.springsecuritywhiteship.login;

import static org.mockito.ArgumentMatchers.*;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
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
import com.gmoon.springsecuritywhiteship.config.SecurityConfig;
import com.gmoon.springsecuritywhiteship.sample.SampleService;

@Import(SecurityConfig.class)
@WebMvcTest(value = UserController.class)
class LoginControllerTest {

	@Autowired
	private WebApplicationContext context;
	private MockMvc mockMvc;

	@MockBean
	private AccountService accountService;

	@MockBean
	private AccountRepository accountRepository;

	@MockBean
	private SampleService sampleService;

	@MockBean
	private BoardRepository boardRepository;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context)
			.alwaysDo(print())
			.apply(springSecurity())
			.build();
	}

	@DisplayName("/login")
	@Nested
	class FormLoginTest {

		@Test
		void success() throws Exception {
			// given
			String username = RandomStringUtils.randomAlphanumeric(4);
			String password = "123";
			PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
			Account account = Account.newUser(username, passwordEncoder.encode(password));
			when(accountService.loadUserByUsername(any())).thenReturn(account);

			// when
			ResultActions result = mockMvc.perform(formLogin().userParameter("id").user(username).password(password));

			// then
			result.andExpect(authenticated());
		}

		@Test
		void error() throws Exception {
			// given
			String username = RandomStringUtils.randomAlphanumeric(4);
			String password = "123";
			PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
			Account account = Account.newUser(username, passwordEncoder.encode(password));
			when(accountService.loadUserByUsername(any())).thenReturn(account);

			// when
			ResultActions result = mockMvc.perform(
				formLogin().userParameter("id").user(username).password(password + "test"));

			// then
			result.andExpect(unauthenticated());
		}
	}

	@DisplayName("/")
	@Nested
	class RequestIndexPageTest {

		@Test
		void accessToAnonymous() throws Exception {
			// when
			ResultActions result = mockMvc.perform(get("/"));

			// then
			result.andExpect(status().isOk());
		}

		@Test
		void accessToUser() throws Exception {
			// when
			ResultActions result = mockMvc.perform(get("/").with(user("gmoon") // mock already login user
				.roles("USER")));

			// then
			result.andExpect(status().isOk());
		}

		@Test
		void accessToAdmin() throws Exception {
			// when
			ResultActions result = mockMvc.perform(
				get("/").with(user("admin") /* mock already login user*/.roles("ADMIN")));

			// then
			result.andExpect(status().isOk());
		}

		@Test
		@WithAnonymousUser
		void withAnonymousUser() throws Exception {
			// when
			ResultActions result = mockMvc.perform(get("/"));

			// then
			result.andExpect(status().isOk());
		}

		@Test
		@WithMockUser(username = "gmoon", roles = {"USER"})
		void withMockUser() throws Exception {
			// when
			ResultActions result = mockMvc.perform(get("/"));

			// then
			result.andExpect(status().isOk());
		}

	}

	@DisplayName("/admin")
	@Nested
	class RequestAdminPageTest {

		@Test
		void accessToUser() throws Exception {
			// when
			ResultActions result = mockMvc.perform(get("/admin").with(user("gmoon").roles("USER")));

			// then
			// result.andExpect(status().isForbidden());
			result.andExpect(status().is3xxRedirection());
		}

		@Test
		void accessToAdmin() throws Exception {
			// when
			ResultActions result = mockMvc.perform(get("/admin").with(user("admin").roles("ADMIN")));

			// then
			result.andExpect(status().isOk());
		}

		@Test
		@WithMockUser(username = "admin", roles = {"ADMIN"})
		void withMockAdmin() throws Exception {
			// when
			ResultActions result = mockMvc.perform(get("/admin"));

			// then
			result.andExpect(status().isOk());
		}

		@Test
		@WithMockUser(username = "user", roles = {"USER"})
		void withMockUser() throws Exception {
			// when
			ResultActions result = mockMvc.perform(get("/admin"));

			// then
			// result.andExpect(status().isForbidden());
			result.andExpect(status().is3xxRedirection());
		}
	}
}
