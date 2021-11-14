package com.gmoon.springsecuritycsrfaspect.csrf.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gmoon.springsecuritycsrfaspect.csrf.token.BaseCsrfToken;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CsrfTokenResponse {

  @JsonProperty
  private final String value;

  public static CsrfTokenResponse ok(BaseCsrfToken token) {
    return new CsrfTokenResponse(token.getValue());
  }
}
