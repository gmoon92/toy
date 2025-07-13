package com.gmoon.commons.commonsapachepoi.users;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;

import com.gmoon.commons.commonsapachepoi.common.base.CodeBasedEnum;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Role implements GrantedAuthority, CodeBasedEnum<String> {
	ADMIN("ROLE_ADMIN"),
	MANAGER("ROLE_MANAGER"),
	USER("ROLE_USER");

	private final String authority;

	private static final Map<String, Role> All = Arrays.stream(values())
		 .collect(Collectors.collectingAndThen(
			  Collectors.toMap(
				   Role::getAuthority,
				   Function.identity()
			  ),
			  Collections::unmodifiableMap
		 ));

	public static Role from(String authority) {
		return All.get(authority);
	}

	@Override
	public String getCode() {
		return name();
	}
}
