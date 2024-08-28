package com.gmoon.jacoco.users.application;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.jacoco.users.domain.User;
import com.gmoon.jacoco.users.domain.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	@Transactional
	public void updatePassword(String userId, String password, String newPassword) {
		User user = userRepository.get(userId);
		if (!passwordEncoder.matches(password, user.getPassword())) {
			throw new IllegalArgumentException("Wrong password.");
		}

		String encoded = passwordEncoder.encode(newPassword);
		user.updatePassword(encoded);

		notifyPasswordChanged(user);
	}

	private void notifyPasswordChanged(User user) {
		if (user.allowReceivingMail()) {
			sendMailChangePasswordAlarm(user);
		}
	}

	private void sendMailChangePasswordAlarm(User user) {
		log.info("{} password has been changed.", user.getUsername());
	}

	@Override
	public UserDetails loadUserByUsername(String username) {
		return userRepository.findByUsername(username)
			 .orElseThrow(() -> new UsernameNotFoundException(username));
	}
}
