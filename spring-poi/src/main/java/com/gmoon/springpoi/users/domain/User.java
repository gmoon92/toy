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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@Entity
@AllArgsConstructor
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

	@Column(nullable = false)
	private Integer gender;

	@Column(length = ColumnLength.EMAIL, nullable = false)
	private String email;

	@Column(nullable = false)
	private boolean enabled;

	public static UserBuilder builder(String username, String password, Role role) {
		return new UserBuilder()
			 .username(username)
			 .password(password)
			 .role(role)
			 .enabled(true);
	}

	public boolean isAdmin() {
		return Role.ADMIN == role;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(role);
	}

	@Override
	public boolean isAccountNonExpired() {
		return enabled;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return enabled;
	}

	@Override
	public boolean isAccountNonLocked() {
		return enabled;
	}
}
