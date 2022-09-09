package com.gmoon.javacore.test.domain;

import com.gmoon.javacore.persistence.Entity;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Entity
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User extends BaseUser {

	private String name;

	public User(UUID id) {
		this.id = id;
	}
}
