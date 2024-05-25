package com.gmoon.resourceserver.jwt;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
	private final MappingJackson2HttpMessageConverter jacksonConverter;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws
		 IOException,
		 ServletException {
		log.warn("unauthorized exception.", e);
		writeJsonResponse(response, e, HttpStatus.UNAUTHORIZED);
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws
		 IOException,
		 ServletException {
		log.warn("authorization exception.", e);
		writeJsonResponse(response, e, HttpStatus.FORBIDDEN);
	}

	private void writeJsonResponse(HttpServletResponse response, Throwable e, HttpStatus httpStatus) throws
		 IOException {
		response.setStatus(httpStatus.value());

		Map<String, Object> body = new HashMap<>();
		body.put("status", httpStatus);
		body.put("message", e.getMessage());
		jacksonConverter.write(body, MediaType.APPLICATION_JSON, new ServletServerHttpResponse(response));
	}
}

