package com.gmoon.springsecuritycsrfaspect.login;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.gmoon.springsecuritycsrfaspect.csrf.annotation.CSRFTokenGenerator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
	@Override
	@CSRFTokenGenerator
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {
		UserDetails user = (UserDetails)authentication.getPrincipal();
		String username = user.getUsername();
		log.info("success login {}...", username);
	}
}
