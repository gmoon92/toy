package com.gmoon.springsecurity.form;

import com.gmoon.springsecurity.account.AccountContext;
import com.gmoon.springsecurity.account.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Slf4j
@Service
public class SampleService {

  public void dashboard() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    // 사용자 정보
    Object principal = authentication.getPrincipal();

    // 사용자 권한
    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

    // 이미 인증을 했기 때문에 null
    Object credentials = authentication.getCredentials();

    // true
    boolean authenticated = authentication.isAuthenticated();

    Member member = AccountContext.getMember();
    log.debug("AccountContext # member : {}", member.getUsername());
  }
}
