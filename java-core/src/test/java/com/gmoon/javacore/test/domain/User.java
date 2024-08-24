package com.gmoon.javacore.test.domain;

import java.util.UUID;

import com.gmoon.javacore.persistence.Entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Entity
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class User extends BaseUser {

	private String name;

	public User(UUID id) {
		this.id = id;
	}
}
