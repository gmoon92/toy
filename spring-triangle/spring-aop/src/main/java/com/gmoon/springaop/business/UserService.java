package com.gmoon.springaop.business;

public interface UserService {
	void updateLevels();

	UserVO update(UserVO user, String name);
}
