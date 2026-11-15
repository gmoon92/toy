package com.gmoon.stampedeguard.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Transactional
@Service
@RequiredArgsConstructor
public class UserCommandService {

	private final UserRepository userRepository;

	public Long register(String username, String email) {
		return userRepository.save(User.of(username, email)).getId();
	}
}
