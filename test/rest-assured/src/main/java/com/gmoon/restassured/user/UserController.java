package com.gmoon.restassured.user;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
	private final UserService service;

	@GetMapping("/list")
	public ResponseEntity<List<User>> getAll() {
		List<User> all = service.findAll();
		return ResponseEntity.ok(all);
	}

	@GetMapping
	public ResponseEntity<User> get(String username) {
		User user = service.find(username);
		return ResponseEntity.ok(user);
	}

	@PostMapping("/{username}")
	public ResponseEntity<User> save(@PathVariable String username) {
		return ResponseEntity.status(HttpStatus.CREATED)
			 .body(service.save(username));
	}

	@DeleteMapping("/{username}")
	public ResponseEntity<Void> delete(@PathVariable String username) {
		service.delete(username);
		return ResponseEntity.status(HttpStatus.NO_CONTENT)
			 .build();
	}
}
