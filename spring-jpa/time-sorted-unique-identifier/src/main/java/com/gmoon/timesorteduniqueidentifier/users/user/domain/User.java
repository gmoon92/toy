package com.gmoon.timesorteduniqueidentifier.users.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.util.Objects;

@Builder(access = AccessLevel.MODULE)
@Table(name = "tb_user")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PUBLIC, staticName = "of")
@Getter
public class User {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.TIME)
	private String id;

	@Column(unique = true, nullable = false)
	private String username;

	@Column(nullable = false)
	private String password;

	@CreatedDate
	private Instant createdAt;

	public User(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public User updatePassword(String newPassword) {
		if (Objects.isNull(newPassword)) {
			throw new IllegalArgumentException("Password cannot be null");
		}

		if (newPassword.equals(password)) {
			throw new IllegalArgumentException("matches your previous password.");
		}

		if (newPassword.length() < 8) {
			throw new IllegalArgumentException("Password must be at least 8 characters");
		}

		password = newPassword;
		return this;
	}
}
