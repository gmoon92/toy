package com.gmoon.springpoi.users.application;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.springpoi.users.domain.User;
import com.gmoon.springpoi.users.infra.UserRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByUsername(username)
			 .orElseThrow(() -> new UsernameNotFoundException("Not found username"));
	}

	public User get(String userId) {
		return userRepository.getReferenceById(userId);
	}
}
