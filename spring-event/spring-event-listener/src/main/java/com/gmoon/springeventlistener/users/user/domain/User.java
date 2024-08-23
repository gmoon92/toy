package com.gmoon.springeventlistener.users.user.domain;

import java.io.Serializable;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User implements Serializable {

	@Id
	@UuidGenerator
	@Column(name = "id", length = 50)
	private String id;

	@Column(name = "name", length = 10, nullable = false)
	private String name;

	@Column(name = "email", length = 20, nullable = false)
	private String email;
}
