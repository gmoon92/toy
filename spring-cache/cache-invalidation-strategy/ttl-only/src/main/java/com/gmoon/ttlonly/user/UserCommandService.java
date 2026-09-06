package com.gmoon.ttlonly.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserCommandService {

	private final UserRepository userRepository;

	@Transactional
	public Long register(String username, String email) {
		return userRepository.save(User.of(username, email)).getId();
	}

	@Transactional
	public void changeEmail(Long id, String email) {
		userRepository.findById(id)
			 .orElseThrow(() -> new IllegalArgumentException("User not found: " + id))
			 .changeEmail(email);
	}
}
