package com.gmoon.springsecurity.sample;

import com.gmoon.springsecurity.account.Account;
import com.gmoon.springsecurity.account.AccountContext;
import com.gmoon.springsecurity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
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

    Account account = AccountContext.getAccount();
    log.debug("AccountContext # account : {}", account.getUsername());
  }

  @Async
  public void async() {
    SecurityUtils.logging("async service");
  }
}
