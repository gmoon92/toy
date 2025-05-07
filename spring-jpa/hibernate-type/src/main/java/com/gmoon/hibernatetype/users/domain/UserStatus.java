package com.gmoon.hibernatetype.users.domain;

import com.gmoon.hibernatetype.global.converter.CodeBasedEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum UserStatus implements CodeBasedEnum {

	ACTIVE(0),
	STANDBY(1),
	DISABLED(2),
	BLOCKED(3);

	private final int code;
}
