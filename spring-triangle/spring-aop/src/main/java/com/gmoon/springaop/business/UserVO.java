package com.gmoon.springaop.business;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class UserVO {
	private long id;
	private String name;
	private Level level;

	public UserVO(long id, String name, Level level) {
		this.id = id;
		this.name = name;
		this.level = level;
	}
}
