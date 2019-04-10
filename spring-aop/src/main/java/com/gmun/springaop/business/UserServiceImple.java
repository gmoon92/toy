package com.gmun.springaop.business;

public class UserServiceImple implements UserService{

private UserVO user;
	
	public UserServiceImple(UserVO user) {
		this.user = user;
	}

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
