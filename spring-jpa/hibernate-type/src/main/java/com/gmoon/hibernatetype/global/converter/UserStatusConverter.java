package com.gmoon.hibernatetype.global.converter;

import com.gmoon.hibernatetype.users.domain.UserStatus;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserStatusConverter extends CodeBasedEnumConverter<UserStatus> {

	public UserStatusConverter() {
		super(UserStatus.class);
	}
}
