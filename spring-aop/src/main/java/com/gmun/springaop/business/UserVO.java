package com.gmun.springaop.business;

import lombok.Data;

@Data
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
