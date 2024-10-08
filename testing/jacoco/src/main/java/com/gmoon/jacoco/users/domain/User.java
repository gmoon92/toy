package com.gmoon.jacoco.users.domain;

import java.util.Collection;
import java.util.List;

import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "tb_user")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User implements UserDetails {

	@Id
	@UuidGenerator
	@Column(length = 50)
	@EqualsAndHashCode.Include
	private String id;

	@Enumerated(EnumType.STRING)
	@Column(length = 10, nullable = false)
	private Role role;

	@Column
	private String username;

	@Column
	private String password;

	@Column
	private boolean enabled;

	@OneToOne(
		 mappedBy = "user",
		 optional = false,
		 cascade = {CascadeType.PERSIST, CascadeType.REMOVE}
	)
	private UserOption userOption;

	@Builder
	public User(String id, Role role, String username, String password, boolean enabled, UserOption userOption) {
		this.id = id;
		this.role = role;
		this.username = username;
		this.password = password;
		this.enabled = enabled;
		this.userOption = userOption;
	}

	public void updatePassword(String newPassword) {
		password = newPassword;
	}

	public boolean allowReceivingMail() {
		return userOption.isAllowsReceivingMail();
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
	public boolean isAccountNonLocked() {
		return enabled;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return enabled;
	}
}
