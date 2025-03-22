package com.rsupport.rv5x.springaccesslog.users.domain;

import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString
public class User {

	private String id;
	private String username;
	private String password;
	private int age;
}
