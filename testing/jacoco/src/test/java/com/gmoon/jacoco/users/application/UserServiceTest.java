package com.gmoon.jacoco.users.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Test
	void updatePassword() {
		UserService service = Mockito.mock(UserService.class);

		service.updatePassword("admin", "password", "newPassword");
	}
}
