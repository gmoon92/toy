package com.gmoon.resourceserver.jwt;

import java.io.IOException;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.gmoon.resourceserver.constants.HttpHeaders;
import com.gmoon.resourceserver.user.User;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	private final JwtUtils jwtUtils;

	public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
		super(authenticationManager);
		this.jwtUtils = jwtUtils;
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
		 Authentication authentication) throws IOException, ServletException {
		User user = (User)authentication.getPrincipal();

		String token = jwtUtils.generate(user);
		response.setHeader(HttpHeaders.AUTHORIZATION, token);
	}

	// todo: login failure logic
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
		 AuthenticationException e) throws IOException, ServletException {
		log.warn("access unauthenticated user.");
		super.unsuccessfulAuthentication(request, response, e);
	}
}
