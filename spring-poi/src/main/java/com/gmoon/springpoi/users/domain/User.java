package com.gmoon.springpoi.users.domain;

import java.io.Serial;
import java.util.Collection;
import java.util.List;

import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.gmoon.springpoi.base.persistence.constants.ColumnLength;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class User implements UserDetails {

	@Serial
	private static final long serialVersionUID = -4039113473933391296L;

	@Id
	@UuidGenerator(style = UuidGenerator.Style.TIME)
	@Column(length = ColumnLength.ID)
	private String id;

	@Column(length = ColumnLength.USERNAME, nullable = false)
	private String username;

	@Column(length = ColumnLength.PASSWORD_BCRYPT, nullable = false)
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(length = ColumnLength.ENUM, nullable = false)
	private Role role;

	public User(String username, String password, Role role) {
		this.username = username;
		this.password = password;
		this.role = role;
	}

	public boolean isAdmin() {
		return Role.ADMIN == role;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(role);
	}
}
