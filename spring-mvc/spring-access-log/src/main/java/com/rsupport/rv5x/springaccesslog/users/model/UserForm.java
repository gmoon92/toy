package com.rsupport.rv5x.springaccesslog.users.model;

import com.rsupport.rv5x.springaccesslog.users.domain.User;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class UserForm {

	private String username;
	private Integer age;

	public UserForm(User user) {
		username = user.getUsername();
		age = user.getAge();
	}
}
