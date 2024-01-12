package com.gmoon.springintegrationamqp.users.user.domain;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
@EqualsAndHashCode
public class User implements Serializable {

	private static final long serialVersionUID = 7484348880855187871L;

	private String email;
	private String username;
	private String password;
	private Role role;

	@Builder
	private User(String email, String username, String password, Role role) {
		this.email = email;
		this.username = username;
		this.password = password;
		this.role = role;
	}
}
