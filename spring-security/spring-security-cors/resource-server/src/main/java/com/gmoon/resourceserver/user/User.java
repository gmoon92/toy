package com.gmoon.resourceserver.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.core.userdetails.UserDetails;

import com.gmoon.resourceserver.util.SecurityUtils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Entity
@Getter
@Table(name = "tb_user")
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(of = "username")
public class User implements UserDetails {
	@Id
	@GeneratedValue
	private Long id;

	@ToString.Include
	private String username;

	private String password;

	@Enumerated(value = EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Role role;

	@ToString.Include
	@ColumnDefault("1")
	@Column(name = "enabled", nullable = false)
	private boolean enabled;

	@ColumnDefault("1")
	@Column(name = "account_non_expired", nullable = false)
	private boolean accountNonExpired;

	@ColumnDefault("1")
	@Column(name = "account_non_locked", nullable = false)
	private boolean accountNonLocked;

	@ColumnDefault("1")
	@Column(name = "credentials_non_expired", nullable = false)
	private boolean credentialsNonExpired;

	@Override
	public Collection<Role> getAuthorities() {
		List<Role> roles = new ArrayList<>();
		roles.add(role);
		return roles;
	}

	public static User create(String username, String password, Role role) {
		User user = new User();
		user.username = username;
		user.password = SecurityUtils.encodePassword(password);
		user.role = role;
		return user;
	}
}
