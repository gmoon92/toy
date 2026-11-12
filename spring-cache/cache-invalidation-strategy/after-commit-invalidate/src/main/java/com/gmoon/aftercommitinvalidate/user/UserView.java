package com.gmoon.aftercommitinvalidate.user;

public record UserView(Long id, String username, String email) {

	public static UserView from(User user) {
		return new UserView(user.getId(), user.getUsername(), user.getEmail());
	}
}
