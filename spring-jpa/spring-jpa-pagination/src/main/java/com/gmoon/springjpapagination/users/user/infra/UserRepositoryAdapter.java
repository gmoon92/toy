package com.gmoon.springjpapagination.users.user.infra;

import static com.gmoon.springjpapagination.users.user.domain.QUser.*;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.JoinExpression;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.QueryModifiers;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberOperation;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import com.gmoon.javacore.util.StringUtils;
import com.gmoon.springjpapagination.users.user.domain.UserRepository;
import com.gmoon.springjpapagination.users.user.dto.QUserContentVO;
import com.gmoon.springjpapagination.users.user.dto.UserContentVO;
import com.gmoon.springjpapagination.users.user.dto.UserContentVO.Type;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

	private final JPAQueryFactory queryFactory;
	private final JpaUserRepository repository;

	@Override
	public List<UserContentVO> getUserContents(String groupId, String keyword, Pageable pageable) {
		return getUserContentQuery(groupId, keyword, pageable)
			.fetch();
	}

	private JPAQuery<UserContentVO> getUserContentQuery(String groupId, String keyword, Pageable pageable) {
		return pagingQuery(pageable)
			.select(new QUserContentVO(
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

	private JPAQuery<?> pagingQuery(Pageable pageable) {
		JPAQuery<?> query = queryFactory.query();
		if (pageable.isPaged()) {
			query.limit(pageable.getPageSize())
				.offset(pageable.getOffset());
		}

		return query;
	}

	@Override
	public long getUserContentTotalCount(String groupId, String keyword) {
		return getCount(
			getUserContentQuery(
				groupId,
				keyword,
				Pageable.unpaged()
			)
		);
	}

	private <T> Long getCount(JPAQuery<T> query) {
		JPAQuery<T> countQuery = query.clone();

		// clear order by
		QueryMetadata metadata = countQuery.getMetadata();
		metadata.clearOrderBy();

		// clear limit offset
		metadata.setModifiers(QueryModifiers.EMPTY);

		return countQuery.select(
			getCountExpression(metadata)
		).fetchFirst();
	}

	private NumberOperation<Long> getCountExpression(QueryMetadata metadata) {
		Expression<?> from = metadata.getJoins()
			.stream()
			.map(JoinExpression::getTarget)
			.filter(this::isRootPath)
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("Not found from expression."));

		return Expressions.numberOperation(
			Long.class,
			Ops.AggOps.COUNT_AGG,
			from
		);
	}

	private boolean isRootPath(Expression<?> expr) {
		return expr instanceof Path && ((Path<?>)expr).getMetadata().isRoot();
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
