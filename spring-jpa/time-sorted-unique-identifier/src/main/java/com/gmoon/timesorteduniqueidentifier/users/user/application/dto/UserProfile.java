package com.gmoon.timesorteduniqueidentifier.users.user.application.dto;

import com.gmoon.timesorteduniqueidentifier.users.user.domain.User;

public record UserProfile(String id, String username, long createdTime) {

	public UserProfile(User user) {
		this(user.getId(), user.getUsername(), user.getCreatedAt().toEpochMilli());
	}
}
