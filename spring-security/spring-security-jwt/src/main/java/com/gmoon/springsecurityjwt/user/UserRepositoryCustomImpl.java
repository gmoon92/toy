package com.gmoon.springsecurityjwt.user;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom {
	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Optional<User> findAdminUser() {
		QUser user = QUser.user;
		User admin = jpaQueryFactory.select(user)
			.from(user)
			.where(user.role.eq(Role.ADMIN))
			.limit(1)
			.fetchOne();
		return Optional.ofNullable(admin);
	}
}
