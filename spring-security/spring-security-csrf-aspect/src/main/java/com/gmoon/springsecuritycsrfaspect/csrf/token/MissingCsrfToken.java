package com.gmoon.springsecuritycsrfaspect.csrf.token;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.web.csrf.CsrfToken;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MissingCsrfToken implements CsrfToken {
  public static final MissingCsrfToken INSTANCE = new MissingCsrfToken();
  private static final String BLANK_STRING = "";

  @Override
  public String getHeaderName() {
    return BLANK_STRING;
  }

  @Override
  public String getParameterName() {
    return BLANK_STRING;
  }

  @Override
  public String getToken() {
    return BLANK_STRING;
  }
}
