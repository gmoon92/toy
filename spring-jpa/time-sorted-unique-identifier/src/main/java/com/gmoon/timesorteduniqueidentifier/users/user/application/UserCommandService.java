package com.gmoon.timesorteduniqueidentifier.users.user.application;

import com.gmoon.timesorteduniqueidentifier.global.base.CommandUseCase;
import com.gmoon.timesorteduniqueidentifier.users.user.application.port.in.UpdateUserPasswordUseCase;
import com.gmoon.timesorteduniqueidentifier.users.user.application.port.in.UserPasswordUpdateCommand;
import com.gmoon.timesorteduniqueidentifier.users.user.application.port.out.LoadUserPort;
import com.gmoon.timesorteduniqueidentifier.users.user.application.port.out.UpdateUserPasswordPort;
import com.gmoon.timesorteduniqueidentifier.users.user.domain.User;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@CommandUseCase
@RequiredArgsConstructor
class UserCommandService implements UpdateUserPasswordUseCase {

	private final LoadUserPort loadUserPort;
	private final UpdateUserPasswordPort updateUserPasswordPort;

	@Override
	public void updatePassword(String id, UserPasswordUpdateCommand command) {
		String newPassword = command.newPassword();
		Objects.requireNonNull(newPassword);

		User user = loadUserPort.get(id);
		user.updatePassword(newPassword);

		updateUserPasswordPort.save(user);
	}
}
