package com.gmoon.springjpapagination.users.user.infra;

import static com.gmoon.springjpapagination.users.user.domain.QUser.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;

import com.gmoon.javacore.util.StringUtils;
import com.gmoon.springjpapagination.global.domain.BaseRepository;
import com.gmoon.springjpapagination.global.domain.Pageable;
import com.gmoon.springjpapagination.users.user.domain.User;
import com.gmoon.springjpapagination.users.user.domain.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter extends BaseRepository implements UserRepository {

	private final JpaUserRepository repository;

	@Override
	public List<User> findAll(String groupId, String keyword, Pageable pageable) {
		JPAQuery<User> userContentQuery = getUserQuery(groupId, keyword);
		pagination(userContentQuery, pageable);
		return userContentQuery
			.fetch();
	}

	private JPAQuery<User> getUserQuery(String groupId, String keyword) {
		return queryFactory
			.selectFrom(user)
			.where(
				findAssignedGroup(groupId),
				searchKeyword(keyword)
			)
			.orderBy(user.username.desc())
			.clone();
	}

	@Override
	public long countBy(String groupId, String keyword) {
		return countQuery(
			getUserQuery(
				groupId,
				keyword
			)
		);
	}

	private Predicate findAssignedGroup(String groupId) {
		if (StringUtils.isNotBlank(groupId)) {
			return user.userGroup.id.eq(groupId);
		}

		return null;
	}

	private Predicate searchKeyword(String keyword) {
		if (StringUtils.isNotBlank(keyword)) {
			return user.username.like(keyword + "%");
		}

		return null;
	}
}
