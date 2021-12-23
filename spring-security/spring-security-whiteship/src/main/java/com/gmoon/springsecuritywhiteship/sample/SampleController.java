package com.gmoon.springsecuritywhiteship.sample;

import java.util.concurrent.Callable;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.gmoon.springsecuritywhiteship.account.Account;
import com.gmoon.springsecuritywhiteship.account.AccountService;
import com.gmoon.springsecuritywhiteship.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SampleController {

	private final AccountService accountService;
	private final SampleService sampleService;

	@GetMapping("/account/{role}/{username}/{password}")
	public Account createAccount(@ModelAttribute Account member) {
		return accountService.createNew(member);
	}

	@GetMapping("/async-handler")
	@ResponseBody
	public Callable<String> asyncHandler() {
		SecurityUtils.logging("MVC");

		return () -> {
			SecurityUtils.logging("Callable");
			return "Async Handler";
		};
	}

	@GetMapping("/async-service")
	@ResponseBody
	public String asyncService() {
		SecurityUtils.logging("MVC before async service");
		sampleService.async();
		SecurityUtils.logging("MVC after async service");
		return "Async Service";
	}
}
