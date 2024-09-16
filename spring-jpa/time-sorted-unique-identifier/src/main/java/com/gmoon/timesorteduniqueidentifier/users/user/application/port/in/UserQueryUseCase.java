package com.gmoon.timesorteduniqueidentifier.users.user.application.port.in;

import com.gmoon.timesorteduniqueidentifier.users.user.application.dto.UserProfile;

import java.util.List;

public interface UserQueryUseCase {

	List<UserProfile> getAllUsers();
	UserProfile get(String id);
}
