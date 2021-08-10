package com.gmoon.springsecurity.account;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class AccessDeniedController {

  @GetMapping("/access-denied")
  public String denied(Model model, Principal principal) {
    model.addAttribute("name", principal.getName());
    return "access-denied";
  }
}
