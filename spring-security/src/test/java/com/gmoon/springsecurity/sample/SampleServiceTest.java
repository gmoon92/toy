package com.gmoon.springsecurity.sample;

import com.gmoon.springsecurity.account.Account;
import com.gmoon.springsecurity.account.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(SpringRunner.class)
@SpringBootTest
class SampleServiceTest {

  @Autowired
  SampleService sampleService;

  @Autowired
  AccountService accountService;

  @Autowired
  AuthenticationManager authenticationManager;

  @Test
  @DisplayName("시큐리티 메서드로 인한 권한 에러 발생")
  void secured() {
    assertThrows(AuthenticationCredentialsNotFoundException.class
            , () -> sampleService.secured());
  }

  @Test
  @DisplayName("어드민 계정이 로그인되어 있다면 접근 가능")
  void secured_when_admin_login() {
    String credentials = "123";
    Account account = Account.newAdmin("admin", credentials);
    accountService.createNew(account);

    UserDetails userDetails = accountService.loadUserByUsername("admin");
    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetails, credentials);
    Authentication authentication = authenticationManager.authenticate(token);

    SecurityContextHolder.getContext().setAuthentication(authentication);

    sampleService.secured();
  }
}