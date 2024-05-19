package com.gmoon.hibernatetype.users.domain;

import static com.gmoon.hibernatetype.users.domain.QUser.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepositoryQueryImpl implements UserRepositoryQuery {

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
