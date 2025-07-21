package com.gmoon.jacoco.users.application;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Test
	void updatePassword() {
		UserService service = Mockito.mock(UserService.class);

		assertThatCode(() -> service.updatePassword(
			 "admin",
			 "password",
			 "newPassword"
		)).doesNotThrowAnyException();
	}
}
