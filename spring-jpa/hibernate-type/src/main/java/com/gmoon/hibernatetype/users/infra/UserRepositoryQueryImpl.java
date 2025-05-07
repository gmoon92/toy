package com.gmoon.hibernatetype.users.infra;

import com.gmoon.hibernatetype.users.domain.User;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.gmoon.hibernatetype.users.domain.QUser.user;

@RequiredArgsConstructor
class UserRepositoryQueryImpl implements UserRepositoryQuery {

	private final JPAQueryFactory factory;

	@Override
	public List<User> findAllByEncEmail(String email) {

		return factory.select(
				  Projections.constructor(
					   User.class,
					   user.email,
					   user.encEmail
				  )
			 )
			 .from(user)
			 .where(
				  user.encEmail.eq(email)
			 )
			 .fetch();
	}
}
