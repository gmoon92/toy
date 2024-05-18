package com.gmoon.springjpapagination.global.domain.expressions;

import com.querydsl.core.annotations.QueryDelegate;
import com.querydsl.core.types.Predicate;

import com.gmoon.javacore.util.StringUtils;
import com.gmoon.springjpapagination.users.user.domain.QUser;
import com.gmoon.springjpapagination.users.user.domain.User;

public final class UserExpressions {

	@QueryDelegate(User.class)
	public static Predicate likeName(QUser user, String keyword) {
		if (StringUtils.isNotBlank(keyword)) {
			return user.username.like(keyword + "%");
		}

		return null;
	}
}
