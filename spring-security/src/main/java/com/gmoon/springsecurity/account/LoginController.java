package com.gmoon.springsecurity.account;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class LoginController {

  @GetMapping("/login")
  public String loginForm() {
    return "login";
  }

  @GetMapping("/logout")
  public String logoutForm() {
    return "logout";
  }

}
