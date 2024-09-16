package com.gmoon.timesorteduniqueidentifier.users.user.application;

import com.gmoon.timesorteduniqueidentifier.global.base.QueryUseCase;
import com.gmoon.timesorteduniqueidentifier.users.user.application.dto.UserProfile;
import com.gmoon.timesorteduniqueidentifier.users.user.application.port.in.UserQueryUseCase;
import com.gmoon.timesorteduniqueidentifier.users.user.application.port.out.LoadUserPort;
import com.gmoon.timesorteduniqueidentifier.users.user.domain.User;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;

@QueryUseCase
@RequiredArgsConstructor
class UserQueryService implements UserQueryUseCase {

	private final LoadUserPort loadUserPort;

	@Override
	public List<UserProfile> getAllUsers() {
		return loadUserPort.findAll()
			 .stream()
			 .map(UserProfile::new)
			 .sorted(
				  Comparator.comparing(UserProfile::createdTime).reversed()
					   .thenComparing(UserProfile::username)
			 )
			 .toList();
	}

	@Override
	public UserProfile get(String id) {
		User user = loadUserPort.get(id);
		return new UserProfile(user);
	}
}
