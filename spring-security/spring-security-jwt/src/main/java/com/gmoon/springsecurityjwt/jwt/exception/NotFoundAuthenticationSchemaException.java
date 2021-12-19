package com.gmoon.springsecurityjwt.jwt.exception;

public class NotFoundAuthenticationSchemaException extends InvalidAuthTokenException {
  public NotFoundAuthenticationSchemaException(String token) {
    super(String.format("Not found authentication scheme in the requested token. %s.", token));
  }
}
