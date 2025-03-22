package com.rsupport.rv5x.springaccesslog.users.model;

import com.rsupport.rv5x.springaccesslog.users.domain.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserForm {

	private String id;
	private String username;
	private Integer age;

	public UserForm(User user) {
		id = user.getId();
		username = user.getUsername();
		age = user.getAge();
	}
}
