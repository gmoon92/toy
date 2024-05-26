package com.gmoon.resourceserver.jwt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.auth0.jwt.exceptions.JWTVerificationException;

import com.gmoon.javacore.util.ArrayUtils;
import com.gmoon.javacore.util.StringUtils;
import com.gmoon.resourceserver.constants.HttpHeaders;
import com.gmoon.resourceserver.jwt.exception.JwtVerifyException;
import com.gmoon.resourceserver.user.User;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtVerifyFilter extends BasicAuthenticationFilter {
	private final JwtUtils jwtUtils;

	public JwtVerifyFilter(AuthenticationEntryPoint authenticationEntryPoint, JwtUtils jwtUtils) {
		super(authentication -> authentication, authenticationEntryPoint);
		this.jwtUtils = jwtUtils;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws
		 IOException,
		 ServletException {
		String token = getToken(request);
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

	private String getToken(HttpServletRequest request) {
		String cookieToken = getTokenFromCookie(request);
		String headerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
		return StringUtils.defaultIfBlank(cookieToken, headerToken);
	}

	private String getTokenFromCookie(HttpServletRequest request) {
		return getCookies(request)
			 .stream()
			 .filter(cookie -> StringUtils.equals(HttpHeaders.AUTHORIZATION, cookie.getName()))
			 .findFirst()
			 .map(this::getCookieValue)
			 .orElse(StringUtils.EMPTY);
	}

	private List<Cookie> getCookies(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (ArrayUtils.isEmpty(cookies)) {
			return new ArrayList<>();
		}

		return Arrays.asList(cookies);
	}

	private String getCookieValue(Cookie cookie) {
		try {
			String value = cookie.getValue();
			return URLDecoder.decode(value, StandardCharsets.UTF_8.displayName());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("invalid cookie value.", e);
		}
	}
}
