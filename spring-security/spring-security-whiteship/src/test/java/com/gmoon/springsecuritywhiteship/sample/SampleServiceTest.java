package com.gmoon.springsecuritywhiteship.sample;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;

import com.gmoon.springsecuritywhiteship.account.Account;
import com.gmoon.springsecuritywhiteship.account.AccountService;

@SpringBootTest
class SampleServiceTest {

	@Autowired
	private SampleService sampleService;

	@Autowired
	private AccountService accountService;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Test
	@DisplayName("시큐리티 메서드로 인한 권한 에러 발생")
	void secured() {
		assertThrows(AuthenticationCredentialsNotFoundException.class
			, () -> sampleService.secured());
	}

	@Test
	@DisplayName("어드민 계정이 로그인되어 있다면 접근 가능")
	void secured_when_admin_login() {
		String username = RandomStringUtils.randomAlphanumeric(5);
		String credentials = "123";
		Account account = Account.newAdmin(username, credentials);
		accountService.createNew(account);

		UserDetails userDetails = accountService.loadUserByUsername(username);
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetails, credentials);
		Authentication authentication = authenticationManager.authenticate(token);

		SecurityContextHolder.getContext().setAuthentication(authentication);

		sampleService.secured();
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void security_with_mock_user() {
		sampleService.secured();
	}
}
