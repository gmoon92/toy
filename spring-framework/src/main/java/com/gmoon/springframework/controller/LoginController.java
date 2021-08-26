package com.gmoon.springframework.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

  @GetMapping(name = "/")
  public String index() {
    return "/";
  }
}
