package com.gmoon.springpoi.common.excel.vo;

import com.gmoon.springpoi.users.domain.User;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.NONE)
@Getter
public final class ExcelUploadContext {
	private final String userId;
	private final boolean adminUser;

	public ExcelUploadContext(User user) {
		userId = user.getId();
		adminUser = user.isAdmin();
	}
}
