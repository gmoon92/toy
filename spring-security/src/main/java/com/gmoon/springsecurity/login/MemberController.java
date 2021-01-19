package com.gmoon.springsecurity.login;

import com.gmoon.springsecurity.account.AccountService;
import com.gmoon.springsecurity.account.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {

  private final AccountService accountService;

  @GetMapping("/account/{role}/{username}/{password}")
  public Member createAccount(@ModelAttribute Member member) {
    return accountService.createNew(member);
  }
}
