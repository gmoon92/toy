package com.gmoon.timesorteduniqueidentifier.users.user.application;

import com.gmoon.javacore.util.StringUtils;
import com.gmoon.timesorteduniqueidentifier.global.test.InMemoryRepository;
import com.gmoon.timesorteduniqueidentifier.users.user.application.port.in.UserPasswordUpdateCommand;
import com.gmoon.timesorteduniqueidentifier.users.user.application.port.out.LoadUserPort;
import com.gmoon.timesorteduniqueidentifier.users.user.domain.Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class UserCommandServiceTest {

	private UserCommandService service;
	private LoadUserPort loadUserPort;

	@BeforeEach
	void setUp() {
		loadUserPort = new InMemoryRepository();
		service = new UserCommandService(loadUserPort, new InMemoryRepository());
	}

	@Test
	void updatePassword() {
		String id = Fixtures.Users.USER.getId();
		String newPassword = StringUtils.randomAlphabetic(8);

		service.updatePassword(id, new UserPasswordUpdateCommand(newPassword));

		assertThat(loadUserPort.get(id).getPassword()).isEqualTo(newPassword);
	}
}
