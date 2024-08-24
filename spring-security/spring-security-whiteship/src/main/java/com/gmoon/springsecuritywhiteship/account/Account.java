package com.gmoon.springsecuritywhiteship.account;

import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Account implements UserDetails {

	@Id
	@GeneratedValue
	private Long id;

	@Column(unique = true)
	private String username;

	private String password;

	@Enumerated(value = EnumType.STRING)
	private AccountRole role;

	@ColumnDefault("1")
	@Column(nullable = false)
	private boolean enabled;

	@ColumnDefault("0")
	@Column(nullable = false)
	private boolean credentialsExpired;

	@ColumnDefault("0")
	@Column(nullable = false)
	private boolean accountLocked;

	@ColumnDefault("0")
	@Column(nullable = false)
	private boolean accountExpired;

	Account(String username, String password, AccountRole accountRole) {
		this.username = username;
		this.password = password;
		this.role = accountRole;
		this.enabled = true;
		this.credentialsExpired = false;
		this.accountLocked = false;
		this.accountExpired = false;
	}

	public static Account newUser(String username, String password) {
		return new Account(username, password, AccountRole.USER);
	}

	public static Account newAdmin(String username, String password) {
		return new Account(username, password, AccountRole.ADMIN);
	}

	//  java.lang.IllegalArgumentException: There is no PasswordEncoder mapped for the id "null"
	public void encodePassword(PasswordEncoder passwordEncoder) {
		this.password = passwordEncoder.encode(this.password);
	}

	@Override
	public Set<GrantedAuthority> getAuthorities() {
		Set<GrantedAuthority> authorities = new LinkedHashSet<>();
		authorities.add(role);
		return authorities;
	}

	@Override
	public boolean isAccountNonExpired() {
		return !accountExpired;
	}

	@Override
	public boolean isAccountNonLocked() {
		return !accountLocked;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return !credentialsExpired;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public Account getAccount() {
		return this;
	}
}
