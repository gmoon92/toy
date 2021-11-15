package com.gmoon.springsecuritycsrfaspect.csrf;

import com.gmoon.springsecuritycsrfaspect.csrf.token.BaseCsrfToken;
import com.gmoon.springsecuritycsrfaspect.csrf.token.HttpSessionCsrfToken;
import com.gmoon.springsecuritycsrfaspect.csrf.token.MissingCsrfToken;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class CsrfTokenRepository {
  private static final String SESSION_ATTRIBUTE_NAME = "CSRF_TOKEN";

  public void saveTokenOnHttpSession(HttpServletRequest request) {
    BaseCsrfToken newToken = HttpSessionCsrfToken.generate();
    saveToken(request, newToken);
  }

  private void saveToken(HttpServletRequest request, BaseCsrfToken token) {
    boolean missingToken = token == null;
    if (missingToken) {
      token = MissingCsrfToken.INSTANCE;
    }

    request.getSession().setAttribute(getSessionAttributeName(), token);
  }

  public BaseCsrfToken getToken(HttpServletRequest request) {
    boolean invalidatedSession = request.getSession(false) == null;
    if (invalidatedSession) {
      return MissingCsrfToken.INSTANCE;
    }

    BaseCsrfToken token = (BaseCsrfToken) request.getSession().getAttribute(getSessionAttributeName());
    boolean missingToken = token == null;
    if (missingToken) {
      return MissingCsrfToken.INSTANCE;
    }

    return token;
  }

  public String getSessionAttributeName() {
    return SESSION_ATTRIBUTE_NAME;
  }
}
