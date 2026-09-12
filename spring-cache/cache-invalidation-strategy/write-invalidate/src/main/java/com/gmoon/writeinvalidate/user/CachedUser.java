package com.gmoon.writeinvalidate.user;

public record CachedUser(Long id, String username, String email) {

	public static CachedUser from(User user) {
		return new CachedUser(user.getId(), user.getUsername(), user.getEmail());
	}
}
