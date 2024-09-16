package com.gmoon.timesorteduniqueidentifier.users.user.adapter.in.web;

import com.gmoon.timesorteduniqueidentifier.global.base.WebAdapter;
import com.gmoon.timesorteduniqueidentifier.users.user.application.dto.UserProfile;
import com.gmoon.timesorteduniqueidentifier.users.user.application.port.in.UpdateUserPasswordUseCase;
import com.gmoon.timesorteduniqueidentifier.users.user.application.port.in.UserPasswordUpdateCommand;
import com.gmoon.timesorteduniqueidentifier.users.user.application.port.in.UserQueryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@WebAdapter
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

	private final UserQueryUseCase userQueryUseCase;
	private final UpdateUserPasswordUseCase updateUserPasswordUseCase;

	@GetMapping
	public HttpEntity<List<UserProfile>> view() {
		return ResponseEntity.ok(userQueryUseCase.getAllUsers());
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
