package com.gmoon.springjpapagination.global.domain;

import org.springframework.beans.factory.annotation.Autowired;

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

public abstract class BaseRepository {

	@Autowired
	protected JPAQueryFactory queryFactory;

	protected JPAQuery<?> pagingQuery(Pageable pageable) {
		return queryFactory.query()
			 .limit(pageable.getPageSize())
			 .offset(pageable.getOffset());
	}

	protected <T> JPAQuery<T> pagination(JPAQuery<T> query, Pageable pageable) {
		return query
			 .limit(pageable.getPageSize())
			 .offset(pageable.getOffset());
	}

	protected <T> Long countQuery(JPAQuery<T> query) {
		JPAQuery<T> countQuery = query.clone();

		clearOrderBy(countQuery);
		clearLimitOffset(query);

		NumberOperation<Long> countExpression = getCountExpression(query);
		return countQuery.select(
			 countExpression
		).fetchFirst();
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
