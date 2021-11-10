package com.gmoon.springsecuritywhiteship.account;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/signup")
@RequiredArgsConstructor
public class SignUpController {

  private final AccountService accountService;

  @GetMapping
  public String form(Model model) {
    model.addAttribute("account", new Account());
    return "signup";
  }

  @PostMapping
  public String save(String username, String password) {
    accountService.createNew(Account.newUser(username, password));
    return "redirect:/login";
  }
}
