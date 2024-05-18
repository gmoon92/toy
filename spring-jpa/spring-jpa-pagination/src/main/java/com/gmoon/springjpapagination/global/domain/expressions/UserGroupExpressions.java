package com.gmoon.springjpapagination.global.domain.expressions;

import com.querydsl.core.annotations.QueryDelegate;
import com.querydsl.core.types.Predicate;

import com.gmoon.javacore.util.StringUtils;
import com.gmoon.springjpapagination.users.user.domain.QUserGroup;
import com.gmoon.springjpapagination.users.user.domain.UserGroup;

public final class UserGroupExpressions {

	@QueryDelegate(UserGroup.class)
	public static Predicate likeName(QUserGroup userGroup, String keyword) {
		if (StringUtils.isNotBlank(keyword)) {
			return userGroup.name.like(keyword + "%");
		}

		return null;
	}

	@QueryDelegate(UserGroup.class)
	public static Predicate assignedGroup(QUserGroup userGroup, String groupId) {
		if (StringUtils.isNotBlank(groupId)) {
			return userGroup.id.eq(groupId);
		}

		return null;
	}
}
