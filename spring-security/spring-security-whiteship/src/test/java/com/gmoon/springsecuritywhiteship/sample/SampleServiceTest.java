package com.gmoon.springsecuritywhiteship.sample;

import com.gmoon.javacore.util.StringUtils;
import com.gmoon.springsecuritywhiteship.account.Account;
import com.gmoon.springsecuritywhiteship.account.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
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
		String username = StringUtils.randomAlphabetic(5);
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
		assertThatCode(sampleService::secured)
			 .doesNotThrowAnyException();
	}

	/**
	 * deprecated {@link org.springframework.security.access.intercept.aopalliance.MethodSecurityInterceptor}
	 *
	 * @see <a href="https://docs.spring.io/spring-security/reference/migration/index.html">docs.spring.io#migration</a>
	 * @see <a href="https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html">docs.spring.io#spring-security</a>
	 * @see <a href="https://docs.spring.io/spring-security/reference/5.8/migration/servlet/authorization.html#_i_use_a_custom_afterinvocationmanager">docs.spring.io#authorication</a>
	 */
	@Nested
	class AsyncAuthentication {

		@Test
		@WithMockUser(roles = "ADMIN")
		void success() {
			SecurityContext context = SecurityContextHolder.getContext();
			CompletableFuture<Void> future = CompletableFuture.allOf(
				 CompletableFuture.runAsync(() -> DelegatingSecurityContextRunnable.create(() -> sampleService.secured(), context)),
				 CompletableFuture.runAsync(() -> DelegatingSecurityContextRunnable.create(() -> sampleService.secured(), context))
			);

			assertDoesNotThrow(future::join);
		}

		@Test
		@WithMockUser(roles = "ADMIN")
		void failed() {
			SecurityContext context = SecurityContextHolder.getContext();
			CompletableFuture<Void> future = CompletableFuture.allOf(
				 CompletableFuture.runAsync(() -> sampleService.secured()),
				 CompletableFuture.runAsync(() -> DelegatingSecurityContextRunnable.create(() -> sampleService.secured(), context))
			);

			assertThatThrownBy(future::join)
				 .cause()
				 .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
		}
	}
}
