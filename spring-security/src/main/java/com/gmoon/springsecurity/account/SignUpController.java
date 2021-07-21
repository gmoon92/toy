package com.gmoon.springsecurity.account;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
  public String save(@ModelAttribute Account account) {
    account.setRole(AccountRole.USER.getValue());
    accountService.createNew(account);
    return "redirect:/";
  }
}
