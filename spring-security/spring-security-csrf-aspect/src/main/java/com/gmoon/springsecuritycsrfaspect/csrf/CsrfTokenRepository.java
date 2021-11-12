package com.gmoon.springsecuritycsrfaspect.csrf;

import com.gmoon.springsecuritycsrfaspect.csrf.token.HttpSessionCsrfToken;
import com.gmoon.springsecuritycsrfaspect.csrf.token.MissingCsrfToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Slf4j
public class CsrfTokenRepository {
  private static final String SESSION_ATTRIBUTE_NAME = "CSRF_TOKEN";

  private final HttpSessionCsrfTokenRepository csrfTokenRepository;

  public CsrfTokenRepository() {
    csrfTokenRepository = new HttpSessionCsrfTokenRepository();
    csrfTokenRepository.setSessionAttributeName(SESSION_ATTRIBUTE_NAME);
  }

  public void saveTokenOnHttpSession(HttpServletRequest request) {
    CsrfToken newCsrfToken = HttpSessionCsrfToken.generate();
    saveToken(request, newCsrfToken);
  }

  private void saveToken(HttpServletRequest request, CsrfToken csrfToken) {
    if (csrfToken == null) {
      csrfToken = MissingCsrfToken.INSTANCE;
    }
    csrfTokenRepository.saveToken(csrfToken, request, null);
  }

  public CsrfToken getToken(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    return getToken(session);
  }

  private CsrfToken getToken(HttpSession session) {
    if (session == null) {
      return MissingCsrfToken.INSTANCE;
    }

    CsrfToken csrfToken = (CsrfToken) session.getAttribute(getSessionAttributeName());
    if (csrfToken == null) {
      return MissingCsrfToken.INSTANCE;
    }
    return csrfToken;
  }

  public String getTokenValue(HttpSession session) {
    return getToken(session).getToken();
  }

  public String getSessionAttributeName() {
    return SESSION_ATTRIBUTE_NAME;
  }
}
