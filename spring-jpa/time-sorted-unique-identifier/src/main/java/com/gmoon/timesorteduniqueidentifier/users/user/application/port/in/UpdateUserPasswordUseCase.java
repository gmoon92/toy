package com.gmoon.timesorteduniqueidentifier.users.user.application.port.in;

public interface UpdateUserPasswordUseCase {

	void updatePassword(String id, UserPasswordUpdateCommand command);
}
