package com.gmoon.springsecurity.account;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountRole {
  ADMIN("ADMIN", "ROLE_ADMIN"),
  USER("USER", "ROLE_USER");

  private final String value;
  private final String name;
}
