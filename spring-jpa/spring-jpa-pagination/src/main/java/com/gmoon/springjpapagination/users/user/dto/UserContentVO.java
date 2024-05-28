package com.gmoon.springjpapagination.users.user.dto;

import java.io.Serializable;
import java.util.List;

import com.gmoon.springjpapagination.users.user.domain.User;
import com.gmoon.springjpapagination.users.user.domain.UserGroup;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class UserContentVO implements Serializable {

	private static final long serialVersionUID = -2025693400133303082L;

	private Type type;
	private String id;
	private String name;

	public UserContentVO(User user) {
		this.type = Type.USER;
		this.id = user.getId();
		this.name = user.getUsername();
	}

	public UserContentVO(UserGroup userGroup) {
		this.type = Type.USER_GROUP;
		this.id = userGroup.getId();
		this.name = userGroup.getName();
	}

	public UserContentVO(List<UserGroup> userGroups) {

	}

	public enum Type {
		USER_GROUP, USER
	}
}
