package com.gmoon.springintegrationamqp.users.user.domain;

import java.io.Serializable;

import lombok.Getter;

@Getter
public class User implements Serializable {

	private String email;
	private String username;
	private String password;
	private Role role;

}
