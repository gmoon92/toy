package com.gmoon.springframework.controller;

import java.util.Arrays;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/cookie")
public class CookieController {

	@GetMapping("/create/{name}")
	public String create(Model model, @PathVariable("name") String cookieName,
		 HttpServletResponse httpServletResponse) {
		log.info("create cookie start...");
		String cookieValue = "data";
		Cookie newCookie = new Cookie(cookieName, cookieValue);
		int oneDayToMillionSeconds = 60 * 60 * 24;
		newCookie.setMaxAge(oneDayToMillionSeconds);
		httpServletResponse.addCookie(newCookie);

		model.addAttribute("name", cookieName);
		model.addAttribute("value", cookieValue);
		log.info("create cookie end... name: {}, value: {}", cookieName, cookieValue);
		return "cookie-info";
	}

	@GetMapping("/info")
	public String info(Model model, @CookieValue(value = "auth") Cookie cookie) {
		String name = cookie.getName();
		String value = cookie.getValue();
		log.info("info cookie name: {}, value: {}", name, value);
		model.addAttribute("name", name);
		model.addAttribute("value", value);
		return "cookie-info";
	}

	@GetMapping("/req-info/{cookieName}")
	public String httpServletRequestCookieInfo(Model model, HttpServletRequest httpServletRequest,
		 @PathVariable(value = "cookieName") String name) {
		Cookie cookie = Arrays.stream(httpServletRequest.getCookies())
			 .filter(requestCookie -> requestCookie.getName().equals(name))
			 .findFirst()
			 .orElseGet(() -> new Cookie(name, ""));

		String value = cookie.getValue();
		log.info("info cookie name: {}, value: {}", name, value);
		model.addAttribute("name", name);
		model.addAttribute("value", value);
		return "cookie-info";
	}
}
