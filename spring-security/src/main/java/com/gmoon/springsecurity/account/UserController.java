package com.gmoon.springsecurity.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

  @Autowired
  private AccountRepository accountRepository;

  @GetMapping("/user/list")
  public String list(Model model) {
    model.addAttribute("list", accountRepository.findAll());
    return "user/list";
  }
}
