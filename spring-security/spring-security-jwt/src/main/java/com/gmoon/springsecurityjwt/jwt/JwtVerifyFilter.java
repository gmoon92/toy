package com.gmoon.springsecurityjwt.jwt;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.auth0.jwt.exceptions.JWTVerificationException;

import com.gmoon.springsecurityjwt.jwt.exception.JwtVerifyException;
import com.gmoon.springsecurityjwt.user.User;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtVerifyFilter extends BasicAuthenticationFilter {
	private final JwtUtil jwtUtil;

	public JwtVerifyFilter(AuthenticationEntryPoint authenticationEntryPoint, JwtUtil jwtUtils) {
		super(authentication -> authentication, authenticationEntryPoint);
		this.jwtUtil = jwtUtils;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws
		 IOException,
		 ServletException {
		String token = request.getHeader(JwtAuthenticationFilter.HEADER_NAME);
		try {
			User user = jwtUtil.decode(token);

			Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(authentication);
			chain.doFilter(request, response);
		} catch (JWTVerificationException e) {
			SecurityContextHolder.clearContext();
			getAuthenticationEntryPoint().commence(request, response, new JwtVerifyException(e));
		}
	}
}
