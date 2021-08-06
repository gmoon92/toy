package com.gmoon.springsecurity.account;

import com.gmoon.springsecurity.sample.SampleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class LoginController {

  private final SampleService sampleService;
  private final AccountRepository accountRepository;

  @GetMapping("/login")
  public String loginForm() {
    return "login";
  }

  @GetMapping("/logout")
  public String logoutForm() {
    return "logout";
  }

  @GetMapping("/")
  public String index(Model model, Principal principal) {
    String message;
    if (principal == null) {
      message = "Hello, Spring Security";
    } else {
      message = principal.getName();
    }

    model.addAttribute("message", String.format("Hello, %s", message));
    return "index";
  }

  @GetMapping("/info")
  public String info(Model model) {
    model.addAttribute("message", "Hello, Spring Security");
    return "info";
  }

  @GetMapping("/dashboard")
  public String dashboard(Model model, Principal principal) {
    model.addAttribute("message", "Hello, Spring Security" + principal.getName());
    AccountContext.setAccount(accountRepository.findByUsername(principal.getName()));

    sampleService.dashboard();
    return "dashboard";
  }

  @GetMapping("/admin")
  public String admin(Model model, Principal principal) {
    model.addAttribute("message", "Hello, Admin" + principal.getName());
    return "admin";
  }

  @GetMapping("/user")
  public String user(Model model, Principal principal) {
    model.addAttribute("message", "Hello, User" + principal.getName());
    return "user";
  }
}
