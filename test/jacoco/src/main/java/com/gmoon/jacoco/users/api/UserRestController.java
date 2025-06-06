package com.gmoon.jacoco.users.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gmoon.jacoco.global.utils.SecurityUtils;
import com.gmoon.jacoco.users.application.UserService;
import com.gmoon.javacore.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserRestController {

	private final UserService userService;

	@PostMapping("/password")
	public ResponseEntity<Void> updatePassword(String password, String newPassword) {
		if (StringUtils.equals(password, newPassword)) {
			throw new IllegalArgumentException("New Password is same to old password.");
		}

		String userId = SecurityUtils.getCurrentUser().getId();
		userService.updatePassword(userId, password, newPassword);
		return ResponseEntity.noContent()
			 .build();
	}
}
