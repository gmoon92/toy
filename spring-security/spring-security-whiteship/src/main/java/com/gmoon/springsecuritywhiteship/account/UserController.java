package com.gmoon.springsecuritywhiteship.account;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.gmoon.springsecuritywhiteship.annotation.CurrentUser;
import com.gmoon.springsecuritywhiteship.board.BoardRepository;
import com.gmoon.springsecuritywhiteship.sample.SampleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UserController {

	private final AccountRepository accountRepository;
	private final SampleService sampleService;
	private final BoardRepository boardRepository;

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
		model.addAttribute("boards", boardRepository.findCurrentUserBoards());
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
