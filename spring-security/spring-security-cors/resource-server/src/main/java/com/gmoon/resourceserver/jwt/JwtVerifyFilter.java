package com.gmoon.resourceserver.jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.gmoon.resourceserver.constants.HttpHeaders;
import com.gmoon.resourceserver.jwt.exception.JwtVerifyException;
import com.gmoon.resourceserver.user.User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtVerifyFilter extends BasicAuthenticationFilter {
	private final JwtUtils jwtUtils;

	public JwtVerifyFilter(AuthenticationEntryPoint authenticationEntryPoint, JwtUtils jwtUtils) {
		super(authentication -> authentication, authenticationEntryPoint);
		this.jwtUtils = jwtUtils;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		String token = request.getHeader(HttpHeaders.AUTHORIZATION);
		try {
			User user = jwtUtils.decode(token);

			Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(authentication);
			chain.doFilter(request, response);
		} catch (JWTVerificationException e) {
			SecurityContextHolder.clearContext();
			getAuthenticationEntryPoint().commence(request, response, new JwtVerifyException(e));
		}
	}
}
