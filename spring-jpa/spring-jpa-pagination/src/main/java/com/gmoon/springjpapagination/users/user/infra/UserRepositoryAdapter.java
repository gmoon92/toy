package com.gmoon.springjpapagination.users.user.infra;

import static com.gmoon.springjpapagination.users.user.domain.QUser.*;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import com.gmoon.javacore.util.StringUtils;
import com.gmoon.springjpapagination.users.user.domain.UserRepository;
import com.gmoon.springjpapagination.users.user.dto.QUserContentVO;
import com.gmoon.springjpapagination.users.user.dto.UserContentVO;
import com.gmoon.springjpapagination.users.user.dto.UserContentVO.Type;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

	private final JPAQueryFactory queryFactory;
	private final JpaUserRepository repository;

	@Override
	public List<UserContentVO> getUserContents(String groupId, String keyword, Pageable pageable) {
		return queryFactory.select(new QUserContentVO(
				Expressions.asEnum(Type.USER),
				user.id,
				user.username
			))
			.from(user)
			.where(
				findAssignedGroup(groupId),
				searchKeyword(keyword)
			)
			.limit(pageable.getPageSize())
			.offset(pageable.getOffset())
			.fetch();
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
