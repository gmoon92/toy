package com.gmoon.springsecuritycsrfaspect.csrf;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class CsrfTokenRepository {
  private static final String PARAMETER_NAME = "_csrf";
  private static final String HEADER_NAME = "X-CSRF-TOKEN";
  private static final String SESSION_ATTRIBUTE_NAME = "CSRF_TOKEN";

  private final HttpSessionCsrfTokenRepository csrfTokenRepository;

  public CsrfTokenRepository() {
    csrfTokenRepository = new HttpSessionCsrfTokenRepository();
    csrfTokenRepository.setParameterName(PARAMETER_NAME);
    csrfTokenRepository.setHeaderName(HEADER_NAME);
    csrfTokenRepository.setSessionAttributeName(SESSION_ATTRIBUTE_NAME);
  }

  public void saveTokenOnHttpSession(HttpServletRequest request) {
    CsrfToken newToken = generateToken(request);
    saveToken(request, newToken);
  }

  private CsrfToken generateToken(HttpServletRequest request) {
    return csrfTokenRepository.generateToken(request);
  }

  private void saveToken(HttpServletRequest request, CsrfToken newToken) {
    csrfTokenRepository.saveToken(newToken, request, null);
  }

  public CsrfToken getToken(HttpServletRequest request) {
    return csrfTokenRepository.loadToken(request);
  }

  public String getParameterName() {
    return PARAMETER_NAME;
  }

  public String getHeaderName() {
    return HEADER_NAME;
  }

  public String getSessionAttributeName() {
    return SESSION_ATTRIBUTE_NAME;
  }
}
