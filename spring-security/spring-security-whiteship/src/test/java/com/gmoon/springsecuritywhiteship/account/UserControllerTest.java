package com.gmoon.springsecuritywhiteship.account;

import static org.hamcrest.core.StringContains.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	AccountRepository accountRepository;

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	AccountService accountService;

	@Test
	@DisplayName("Security Chain이랑 같은 환경에서 도는건지")
	@WithMockUser
	public void list() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/user/list")
				.with(csrf()))
			.andExpect(MockMvcResultMatchers.status().is3xxRedirection());
	}

	@Test
	@DisplayName("@AuthenticationPrincipal 테스트")
	void currentUser_annotation_injection_principal_should_set_null_when_anonymous() throws Exception {
		// given
		SecurityContext context = SecurityContextHolder.getContext();
		String principal = "admin";
		String credentials = "123123";
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(principal, credentials);
		token.setDetails(accountService.createNew(Account.newAdmin(principal, credentials)));
		context.setAuthentication(authenticationManager.authenticate(token));

		// when
		mockMvc.perform(MockMvcRequestBuilders.get("/sample/annotation"))
			.andDo(print())
			.andExpect(content().string(containsString(principal)))
			.andExpect(status().isOk())
			.andReturn();
	}
}
