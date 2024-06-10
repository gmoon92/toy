package com.gmoon.jacoco.global;

import java.util.List;

import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import com.gmoon.jacoco.users.domain.Role;
import com.gmoon.jacoco.users.domain.User;

public class CustomWithMockUserSecurityContextFactory implements WithSecurityContextFactory<CustomWithMockUser> {

	@Override
	public SecurityContext createSecurityContext(CustomWithMockUser annotation) {
		Authentication authentication = createAuthentication(annotation);

		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		return context;
	}

	private Authentication createAuthentication(CustomWithMockUser annotation) {
		String username = annotation.userId();
		Role role = annotation.role();

		User principal = User.builder()
			 .id(username)
			 .username(username)
			 .role(role)
			 .build();

		Authentication authentication = new TestingAuthenticationToken(principal, null, List.of(role));
		authentication.setAuthenticated(true);
		return authentication;
	}
}
