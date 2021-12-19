package com.gmoon.springsecurityjwt.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@RequiredArgsConstructor
public enum Role implements GrantedAuthority {
  ADMIN,
  MANAGER,
  USER;

  public String getAuthority() {
    return String.format("ROLE_%s", name());
  }
}
