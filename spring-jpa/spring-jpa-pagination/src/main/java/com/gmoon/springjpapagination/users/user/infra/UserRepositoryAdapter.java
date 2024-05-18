package com.gmoon.springjpapagination.users.user.infra;

import static com.gmoon.springjpapagination.users.user.domain.QUser.*;
import static com.gmoon.springjpapagination.users.user.domain.QUserGroup.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQuery;

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
		return getUserQuery(groupId, keyword, pageable)
			.fetch();
	}

	private JPAQuery<User> getUserQuery(String groupId, String keyword, Pageable pageable) {
		return pagingQuery(pageable)
			.select(user)
			.from(user)
			.join(user.userGroup, userGroup)
			.where(
				userGroup.assignedGroup(groupId),
				user.likeName(keyword)
			)
			.orderBy(user.username.desc());
	}

	@Override
	public long countBy(String groupId, String keyword) {
		return countQuery(
			getUserQuery(
				groupId,
				keyword,
				Pageable.unpaged()
			)
		);
	}
}
