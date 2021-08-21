package com.gmoon.springsecurity.account;

import com.gmoon.springsecurity.annotation.CurrentUser;
import com.gmoon.springsecurity.sample.SampleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UserController {

  private final AccountRepository accountRepository;
  private final SampleService sampleService;

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

  @GetMapping("/dashboard")
  public String dashboard(Model model, Principal principal) {
    model.addAttribute("message", "Hello, Spring Security" + principal.getName());
    AccountContext.setAccount(accountRepository.findByUsername(principal.getName()));

    sampleService.dashboard();
    return "dashboard";
  }

  @GetMapping("/user/list")
  public String list(Model model) {
    model.addAttribute("list", accountRepository.findAll());
    return "user/list";
  }

  @GetMapping("/sample/annotation")
  public String annotation(Model model, @CurrentUser Account loginUser) {
    log.info("loginUser : {}", loginUser);

    model.addAttribute("username", loginUser.getUsername());
    return "annotation";
  }
}
