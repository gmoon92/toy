package com.gmoon.springsecuritywhiteship.account;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class LoginController {

	@GetMapping("/login")
	public String loginForm() {
		return "login";
	}

	@GetMapping("/logout")
	public String logoutForm() {
		return "logout";
	}

}
