package com.gmoon.springaop.business;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
	private final UserVO user;

	@Override
	public void updateLevels() {
		user.setLevel(Level.BASIC);
	}

	@Override
	public UserVO update(UserVO user, String name) {
		user.setName(name);
		return user;
	}
}
