package com.gmoon.springsecurityjwt.jwt;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.gmoon.springsecurityjwt.response.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
	private final MappingJackson2HttpMessageConverter jacksonConverter;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
		log.warn("unauthorized exception.", e);
		writeJsonResponse(response, ApiResponse.of(e, HttpStatus.UNAUTHORIZED));
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
		log.warn("authorization exception.", e);
		writeJsonResponse(response, ApiResponse.of(e, HttpStatus.FORBIDDEN));
	}

	private void writeJsonResponse(HttpServletResponse response, ApiResponse apiResponse) throws IOException {
		HttpStatus httpStatus = apiResponse.getHttpStatus();
		response.setStatus(httpStatus.value());
		jacksonConverter.write(apiResponse, MediaType.APPLICATION_JSON, new ServletServerHttpResponse(response));
	}
}
