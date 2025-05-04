package com.gmoon.springjpapagination.global.domain;

import com.querydsl.core.JoinExpression;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.QueryModifiers;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberOperation;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractJpaRepository implements BaseRepository {

	@Autowired
	protected JPAQueryFactory queryFactory;

	protected JPAQuery<?> fetchAndPaginated(BasePageable pageable) {
		return queryFactory
			 .query()
			 .limit(pageable.getPageSize())
			 .offset(pageable.getOffset());
	}

	protected <T> Long fetchTotalCount(JPAQuery<T> query) {
		JPAQuery<T> countQuery = query.clone();

		clearOrderBy(countQuery);
		clearLimitOffset(countQuery);

		return countQuery
			 .select(getCountExpression(countQuery))
			 .fetchFirst();
	}

	private <T> void clearLimitOffset(JPAQuery<T> query) {
		QueryMetadata metadata = query.getMetadata();
		metadata.setModifiers(QueryModifiers.EMPTY);
	}

	private <T> void clearOrderBy(JPAQuery<T> query) {
		QueryMetadata metadata = query.getMetadata();
		metadata.clearOrderBy();
	}

	private <T> NumberOperation<Long> getCountExpression(JPAQuery<T> query) {
		Expression<?> from = query.getMetadata()
			 .getJoins()
			 .stream()
			 .map(JoinExpression::getTarget)
			 .filter(this::isRootPath)
			 .findFirst()
			 .orElseThrow(() -> new IllegalArgumentException("Not found from expression."));

		return Expressions.numberOperation(
			 Long.class,
			 Ops.AggOps.COUNT_ALL_AGG,
			 from
		);
	}

	private boolean isRootPath(Expression<?> expr) {
		return expr instanceof Path && ((Path<?>)expr).getMetadata().isRoot();
	}
}
