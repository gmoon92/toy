package com.gmoon.springjpapagination.users.user.dto;

import java.io.Serializable;

import com.querydsl.core.annotations.QueryProjection;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class UserContentVO implements Serializable {

	private Type type;
	private String id;
	private String name;

	@QueryProjection
	public UserContentVO(Type type, String id, String name) {
		this.id = id;
		this.type = type;
		this.name = name;
	}

	public enum Type {
		USER_GROUP, USER
	}
}
