package com.rsupport.rv5x.springaccesslog.users.api;

import com.rsupport.rv5x.springaccesslog.users.application.UserService;
import com.rsupport.rv5x.springaccesslog.users.model.UserForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Slf4j
@RequestMapping("/user")
@Controller
@SessionAttributes(value = "userFrom")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping
	public String index(Model model) {
		model.addAttribute("users", userService.getAll());
		return "index";
	}

	@GetMapping("/view")
	public String view(@RequestParam String id, Model model) {
		UserForm userForm = userService.getView(id);
		model.addAttribute("userFrom", userForm);
		return "view";
	}

	@PostMapping
	public RedirectView save(
		 @ModelAttribute UserForm userForm,
		 RedirectAttributes redirectAttributes,
		 SessionStatus status
	) {
		UserForm savedUserForm = userService.save(userForm);
		status.setComplete();

		redirectAttributes.addAttribute("id", savedUserForm.getId());

		RedirectView redirectView = new RedirectView("/users/view");
		redirectAttributes.addFlashAttribute("userForm", savedUserForm);
		return redirectView;
	}

	@PatchMapping
	@ResponseBody
	public HttpEntity<Void> update(@RequestBody UserForm userForm) {
		userService.update(userForm);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping
	@ResponseBody
	public HttpEntity<Void> delete(@RequestParam String id) {
		userService.remove(id);
		return ResponseEntity.noContent().build();
	}
}
