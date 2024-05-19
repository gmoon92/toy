package com.gmoon.springsecurityjwt.jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.gmoon.springsecurityjwt.user.User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	public static final String HEADER_NAME = "Authorization";

	private final JwtUtil jwtUtil;

	public JwtAuthenticationFilter(AuthenticationManager authenticationManager,
		 JwtUtil jwtUtil) {
		super(authenticationManager);
		this.jwtUtil = jwtUtil;
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
		 Authentication authentication) throws IOException, ServletException {
		User user = (User)authentication.getPrincipal();

		String token = jwtUtil.generate(user);
		response.setHeader(HEADER_NAME, token);
	}

	// todo: login failure logic
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
		 AuthenticationException e) throws IOException, ServletException {
		log.warn("access unauthenticated user.");
		super.unsuccessfulAuthentication(request, response, e);
	}
}
