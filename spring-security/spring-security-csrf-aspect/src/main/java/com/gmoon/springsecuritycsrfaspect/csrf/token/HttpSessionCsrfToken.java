package com.gmoon.springsecuritycsrfaspect.csrf.token;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.csrf.CsrfToken;

import java.util.UUID;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpSessionCsrfToken implements CsrfToken {
  private static final String HEADER_NAME = "X-CSRF-TOKEN";
  private static final String PARAMETER_NAME = "_csrf";

  private final String token;

  public static HttpSessionCsrfToken generate() {
    String newToken = UUID.randomUUID().toString();
    return new HttpSessionCsrfToken(newToken);
  }

  @Override
  public String getHeaderName() {
    return HEADER_NAME;
  }

  @Override
  public String getParameterName() {
    return PARAMETER_NAME;
  }

  @Override
  public String getToken() {
    return token;
  }
}
