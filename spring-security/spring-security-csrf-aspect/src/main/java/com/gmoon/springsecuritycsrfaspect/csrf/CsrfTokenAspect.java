package com.gmoon.springsecuritycsrfaspect.csrf;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.web.csrf.CsrfToken;

import com.gmoon.javacore.util.StringUtils;
import com.gmoon.springsecuritycsrfaspect.csrf.token.BaseCsrfToken;
import com.gmoon.springsecuritycsrfaspect.csrf.token.MissingCsrfToken;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@RequiredArgsConstructor
public class CsrfTokenAspect {
	private final HttpServletRequest request;
	private final CsrfTokenRepository csrfTokenRepository;

	@Pointcut("@annotation(com.gmoon.springsecuritycsrfaspect.csrf.annotation.CSRFTokenGenerator)")
	public void csrfTokenGenerator() {
	}

	@AfterReturning("csrfTokenGenerator()")
	public void generator() {
		csrfTokenRepository.saveTokenOnHttpSession(request);
		log.debug("new csrf token created in http session attribute.");
	}

	@Pointcut("within(@org.springframework.stereotype.Controller *)")
	public void controller() {
	}

	@Pointcut("@annotation(com.gmoon.springsecuritycsrfaspect.csrf.annotation.CSRF)")
	public void csrf() {
	}

	@Before("controller() && csrf()")
	public void checkCsrfToken() {
		HttpSession session = request.getSession(false);
		if (session == null) {
			return;
		}

		BaseCsrfToken sessionToken = getSessionTokenOrElseThrow();
		String headerName = sessionToken.getHeaderName();
		String parameterName = sessionToken.getParameterName();

		String requestToken = getRequestTokenOrElseThrow(headerName, parameterName);
		checkRequestCsrfToken(sessionToken, requestToken);
	}

	private BaseCsrfToken getSessionTokenOrElseThrow() {
		BaseCsrfToken sessionToken = csrfTokenRepository.getToken(request);
		if (sessionToken instanceof MissingCsrfToken) {
			throw new InvalidCsrfTokenException("http session csrf token is null.");
		}
		return sessionToken;
	}

	private String getRequestTokenOrElseThrow(String headerName, String parameterName) {
		String requestToken = getRequestToken(headerName, parameterName);
		if (StringUtils.isBlank(requestToken)) {
			throw new InvalidCsrfTokenException("request csrf token is null.");
		}
		return requestToken;
	}

	private String getRequestToken(String headerName, String parameterName) {
		String header = request.getHeader(headerName);
		String parameter = request.getParameter(parameterName);
		return StringUtils.defaultString(header, parameter);
	}

	private void checkRequestCsrfToken(CsrfToken sessionToken, String requestToken) {
		boolean isExistsCRSFToken = StringUtils.equals(sessionToken.getToken(), requestToken);
		if (!isExistsCRSFToken) {
			throw new InvalidCsrfTokenException("csrf attack is prevented");
		}
	}
}
