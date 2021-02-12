package com.gmoon.springsecurity.login;

import com.gmoon.springsecurity.account.AccountContext;
import com.gmoon.springsecurity.account.MemberRepository;
import com.gmoon.springsecurity.form.SampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class LoginController {

  @Autowired
  private SampleService sampleService;

  @Autowired
  private MemberRepository memberRepository;

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
    AccountContext.setMember(memberRepository.findByUsername(principal.getName()));

    sampleService.dashboard();
    return "dashboard";
  }

  @GetMapping("/admin")
  public String admin(Model model, Principal principal) {
    model.addAttribute("message", "Hello, Spring Security" + principal.getName());
    return "admin";
  }

}
