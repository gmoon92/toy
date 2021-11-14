package com.gmoon.springsecuritycsrfaspect.user;

import com.gmoon.springsecuritycsrfaspect.csrf.annotation.CSRF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequestMapping("/user")
public class UserController {

  @GetMapping("/info")
  public String info(Model model, @AuthenticationPrincipal User user) {
    model.addAttribute("name", user.getUsername());
    model.addAttribute("blog", "gmoon92.github.io");
    return "info";
  }

  @CSRF
  @ResponseBody
  @GetMapping("/delete")
  public String delete() {
    log.info("delete...");
    return "delete";
  }
}
