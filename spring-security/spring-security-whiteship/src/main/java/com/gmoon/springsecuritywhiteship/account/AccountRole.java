package com.gmoon.springsecuritywhiteship.account;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@Getter
@RequiredArgsConstructor
public enum AccountRole implements GrantedAuthority {
  ADMIN("ADMIN", "ROLE_ADMIN"),
  USER("USER", "ROLE_USER");

  private final String role;
  private final String authority;
}
