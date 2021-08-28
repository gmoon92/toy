package com.gmoon.springframework.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Controller
public class LoginController {

  @GetMapping("/")
  public String index() {
    return "index";
  }

  @GetMapping("/create-cookie/{key}")
  public String createCookie(Model model, @PathVariable("key") String cookieKey, HttpServletResponse httpServletResponse) {
    log.info("create cookie start...");
    String cookieValue = "anonymous-data";
    Cookie newCookie = new Cookie(cookieKey, cookieValue);
    int oneDayToMillionSeconds = 60 * 60 * 24;
    newCookie.setMaxAge(oneDayToMillionSeconds);
    httpServletResponse.addCookie(newCookie);

    model.addAttribute("key", cookieKey);
    model.addAttribute("value", cookieValue);
    log.info("create cookie end... key: {}, value: {}", cookieKey, cookieValue);
    return "cookie-info";
  }


}
