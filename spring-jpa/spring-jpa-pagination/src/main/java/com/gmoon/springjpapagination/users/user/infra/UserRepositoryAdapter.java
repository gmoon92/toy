package com.gmoon.springjpapagination.users.user.infra;

import static com.gmoon.springjpapagination.users.user.domain.QUser.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;

import com.gmoon.javacore.util.StringUtils;
import com.gmoon.springjpapagination.global.domain.BaseRepository;
import com.gmoon.springjpapagination.global.domain.Pageable;
import com.gmoon.springjpapagination.users.user.domain.UserRepository;
import com.gmoon.springjpapagination.users.user.dto.QUserVO;
import com.gmoon.springjpapagination.users.user.dto.UserVO;
import com.gmoon.springjpapagination.users.user.dto.UserVO.Type;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter extends BaseRepository implements UserRepository {

	private final JpaUserRepository repository;

	@Override
	public List<UserVO> getUserContents(String groupId, String keyword, Pageable pageable) {
		JPAQuery<UserVO> userContentQuery = getUserContentQuery(groupId, keyword);
		pagination(userContentQuery, pageable);
		return userContentQuery
			.fetch();
	}

	private JPAQuery<UserVO> getUserContentQuery(String groupId, String keyword) {
		return queryFactory
			.select(new QUserVO(
				Expressions.asEnum(Type.USER),
				user.id,
				user.username
			))
			.from(user)
			.where(
				findAssignedGroup(groupId),
				searchKeyword(keyword)
			)
			.orderBy(user.username.desc())
			.clone();
	}

	@Override
	public long getUserContentTotalCount(String groupId, String keyword) {
		return countQuery(
			getUserContentQuery(
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