package com.gmoon.springsecurityjwt.user;

import static org.assertj.core.api.BDDAssertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(UserService.class)
class UserServiceTest {
	@Autowired
	UserService userService;

	@MockBean
	UserRepository userRepository;

	@Test
	@DisplayName("UserDetails 를 조회한다.")
	void testLoadUserByUsername() {
		// given
		String username = "admin";

		// when
		when(userRepository.findByUsername(username))
			 .thenReturn(Optional.of(User.create(username, "", Role.ADMIN)));

		// then
		then(userService.loadUserByUsername(username))
			 .isInstanceOf(UserDetails.class)
			 .isInstanceOf(User.class)
			 .isNotNull();
	}

	@Test
	@DisplayName("UserDetails 조회시 값이 없으면 에러가 발생한다.")
	void testLoadUserByUsername_error() {
		// given
		String username = "anonymous";

		// when
		when(userRepository.findByUsername(username))
			 .thenReturn(Optional.empty());

		// then
		assertThatExceptionOfType(UsernameNotFoundException.class)
			 .isThrownBy(() -> userService.loadUserByUsername(username));
	}
}
