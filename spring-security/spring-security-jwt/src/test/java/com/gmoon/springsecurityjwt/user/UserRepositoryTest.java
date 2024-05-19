package com.gmoon.springsecurityjwt.user;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.gmoon.springsecurityjwt.base.BaseDataJpaTest;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class UserRepositoryTest extends BaseDataJpaTest {
	final UserRepository userRepository;

	@Test
	@DisplayName("username 으로 사용자를 찾는다.")
	void testFindByUsername() {
		// given
		String username = "admin";

		// when
		Optional<User> maybeUser = userRepository.findByUsername(username);

		// then
		assertThat(maybeUser).isPresent();
	}

	@Test
	@DisplayName("어드민 계정을 찾는다.")
	void testFindAdminUser() {
		// when
		Optional<User> mayAdminUser = userRepository.findAdminUser();

		// then
		assertThat(mayAdminUser).isPresent()
			 .hasValueSatisfying(user ->
				  assertThat(user.getRole()).isEqualTo(Role.ADMIN));
	}
}
