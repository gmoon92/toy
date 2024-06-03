package com.gmoon.springasync.member;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class Member implements Serializable {
	private static final long serialVersionUID = 455845596925771520L;
	private static final String INIT_PASSWORD = "111111";

	@Id
	@GeneratedValue
	private Long id;

	@Column(name = "email", unique = true, nullable = false)
	private String email;

	@Column(name = "password", nullable = false)
	private String password;

	private Member(String email, String password) {
		this.email = email;
		this.password = password;
	}

	public static Member createNew(String email) {
		return new Member(email, INIT_PASSWORD);
	}
}
