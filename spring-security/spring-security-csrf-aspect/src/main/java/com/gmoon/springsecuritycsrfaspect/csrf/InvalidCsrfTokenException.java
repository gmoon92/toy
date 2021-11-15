package com.gmoon.springsecuritycsrfaspect.csrf;

import org.springframework.security.access.AccessDeniedException;

public class InvalidCsrfTokenException extends AccessDeniedException {

  public InvalidCsrfTokenException(String message) {
    super(message);
  }
}
