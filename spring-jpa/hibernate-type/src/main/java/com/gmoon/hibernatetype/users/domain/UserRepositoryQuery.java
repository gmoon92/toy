package com.gmoon.hibernatetype.users.domain;

import static com.gmoon.hibernatetype.users.domain.QUser.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepositoryQuery implements UserRepositoryCustom {

	private final JPAQueryFactory factory;

	@Override
	public List<User> findAllByEncEmail(String email) {
		return factory.select(user)
			.from(user)
			.where(
				user.encEmail.eq(email)
			)
			.fetch();
	}
}
