package com.gmoon.springsecuritycsrfaspect.csrf;

import com.gmoon.springsecuritycsrfaspect.csrf.token.BaseCsrfToken;
import com.gmoon.springsecuritycsrfaspect.csrf.token.HttpSessionCsrfToken;
import com.gmoon.springsecuritycsrfaspect.csrf.token.MissingCsrfToken;
import lombok.Getter;

import javax.servlet.http.HttpServletRequest;

public class CsrfTokenRepository {
  private static final String SESSION_ATTRIBUTE_NAME = "CSRF_TOKEN";

  @Getter
  private String sessionAttributeName;
  
  public CsrfTokenRepository() {
    sessionAttributeName = SESSION_ATTRIBUTE_NAME;
  }

  public void saveTokenOnHttpSession(HttpServletRequest request) {
    BaseCsrfToken newToken = HttpSessionCsrfToken.generate();
    saveToken(request, newToken);
  }

  private void saveToken(HttpServletRequest request, BaseCsrfToken token) {
    boolean missingToken = token == null;
    if (missingToken) {
      token = MissingCsrfToken.INSTANCE;
    }

    request.getSession().setAttribute(sessionAttributeName, token);
  }

  public BaseCsrfToken getToken(HttpServletRequest request) {
    boolean invalidatedSession = request.getSession(false) == null;
    if (invalidatedSession) {
      return MissingCsrfToken.INSTANCE;
    }

    BaseCsrfToken token = (BaseCsrfToken) request.getSession().getAttribute(sessionAttributeName);
    boolean missingToken = token == null;
    if (missingToken) {
      return MissingCsrfToken.INSTANCE;
    }

    return token;
  }
}
