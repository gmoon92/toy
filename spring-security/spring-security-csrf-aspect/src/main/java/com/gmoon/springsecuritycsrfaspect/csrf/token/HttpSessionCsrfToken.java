package com.gmoon.springsecuritycsrfaspect.csrf.token;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode(of = "value")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpSessionCsrfToken extends BaseCsrfToken {
  private final String value;

  public static HttpSessionCsrfToken generate() {
    String value = UUID.randomUUID().toString();
    return new HttpSessionCsrfToken(value);
  }
}
