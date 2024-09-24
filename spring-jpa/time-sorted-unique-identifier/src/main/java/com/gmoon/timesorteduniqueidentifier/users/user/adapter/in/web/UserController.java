package com.gmoon.timesorteduniqueidentifier.users.user.adapter.in.web;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gmoon.timesorteduniqueidentifier.global.base.WebAdapter;
import com.gmoon.timesorteduniqueidentifier.users.user.application.dto.UserProfile;
import com.gmoon.timesorteduniqueidentifier.users.user.application.port.in.UpdateUserPasswordUseCase;
import com.gmoon.timesorteduniqueidentifier.users.user.application.port.in.UserPasswordUpdateCommand;
import com.gmoon.timesorteduniqueidentifier.users.user.application.port.in.UserQueryUseCase;

import lombok.RequiredArgsConstructor;

@WebAdapter
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

	private final UserQueryUseCase userQueryUseCase;
	private final UpdateUserPasswordUseCase updateUserPasswordUseCase;

	@GetMapping
	public HttpEntity<List<UserProfile>> view() {
		return userQueryUseCase.getAllUsers()
			 .stream()
			 .collect(Collectors.collectingAndThen(Collectors.toList(), ResponseEntity::ok));
	}

	@GetMapping("/{id}")
	public HttpEntity<UserProfile> detail(@PathVariable final String id) {
		UserProfile user = userQueryUseCase.get(id);
		return ResponseEntity.ok(user);
	}

	@PutMapping("/password/{id}")
	public HttpEntity<Void> updatePassword(@PathVariable String id, @RequestBody String newPassword) {
		updateUserPasswordUseCase.updatePassword(id, new UserPasswordUpdateCommand(newPassword));
		return ResponseEntity.noContent().build();
	}
}
