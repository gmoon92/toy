package com.gmoon.springsecurityjwt.user;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import com.gmoon.springsecurityjwt.util.SecurityUtils;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Table(name = "tb_user", uniqueConstraints = {@UniqueConstraint(name = "u_username", columnNames = "username")})
@Entity
@Getter
@EqualsAndHashCode(of = "username")
public class User implements UserDetails {
	@Id
	@GeneratedValue
	private Long id;

	private String username;

	private String password;

	@Enumerated(value = EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Role role;

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

	protected User() {
		enableAccount();
	}

	public static User create(String username, String password, Role role) {
		User user = new User();
		user.username = username;
		user.password = SecurityUtils.encodePassword(password);
		user.role = role;
		return user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return AuthorityUtils.createAuthorityList(role.getAuthority());
	}

	private void enableAccount() {
		enabled = true;
		accountNonExpired = true;
		accountNonLocked = true;
		credentialsNonExpired = true;
	}
}
