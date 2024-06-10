package com.gmoon.jacoco.global.utils;

import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.gmoon.jacoco.users.domain.User;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityUtils {

	public static User getCurrentUser() {
		return obtainUserDetails()
			 .map(userDetails -> (User)userDetails)
			 .orElseThrow(() -> new AccessDeniedException("User not found in security session."));
	}

	private static Optional<UserDetails> obtainUserDetails() {
		AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

		SecurityContext context = SecurityContextHolder.getContext();
		Authentication authentication = context.getAuthentication();
		if (trustResolver.isAuthenticated(authentication)) {
			UserDetails principal = (UserDetails)authentication.getPrincipal();
			return Optional.of(principal);
		}

		return Optional.empty();
	}
}
