package com.gmoon.springsecuritycsrfaspect.user;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {
	private final SecurityProperties securityProperties;
	private final PasswordEncoder passwordEncoder;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		checkLoginUsername(username);

		SecurityProperties.User user = securityProperties.getUser();
		String password = user.getPassword();
		String grantedAuthority = user.getRoles().stream()
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("user authority is not defined."));
		log.info("username: {}, password: {}, grantedAuthority: {}", username, password, grantedAuthority);
		return User.builder()
			.username(username)
			.password(passwordEncoder.encode(password))
			.authorities(String.format("ROLE_%s", grantedAuthority))
			.build();
	}

	private void checkLoginUsername(String username) {
		SecurityProperties.User user = securityProperties.getUser();
		if (!StringUtils.equals(user.getName(), username)) {
			throw new UsernameNotFoundException(String.format("username %s is not found", username));
		}
	}
}
