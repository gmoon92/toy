package com.gmoon.springsecuritycsrfaspect.csrf;

import com.gmoon.springsecuritycsrfaspect.csrf.token.BaseCsrfToken;
import com.gmoon.springsecuritycsrfaspect.csrf.token.MissingCsrfToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.csrf.CsrfToken;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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
      throw new AccessDeniedException("http session csrf token is null.");
    }
    return sessionToken;
  }

  private String getRequestTokenOrElseThrow(String headerName, String parameterName) {
    String requestToken = getRequestToken(headerName, parameterName);
    if (StringUtils.isBlank(requestToken)) {
      throw new AccessDeniedException("request csrf token is null.");
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
      throw new AccessDeniedException("csrf attack is prevented");
    }
  }
}