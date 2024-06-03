package com.gmoon.springsecurityjwt.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.core.userdetails.UserDetails;

import com.gmoon.springsecurityjwt.util.SecurityUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Entity
@Table(
	 name = "tb_user",
	 uniqueConstraints = {
		  @UniqueConstraint(name = "u_username", columnNames = "username")
	 }
)
@Getter
@EqualsAndHashCode(of = "username")
@ToString(onlyExplicitlyIncluded = true)
public class User implements UserDetails {
	private static final long serialVersionUID = -8809792987207423144L;

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
	public Collection<Role> getAuthorities() {
		List<Role> roles = new ArrayList<>();
		roles.add(role);
		return roles;
	}

	private void enableAccount() {
		enabled = true;
		accountNonExpired = true;
		accountNonLocked = true;
		credentialsNonExpired = true;
	}
}
