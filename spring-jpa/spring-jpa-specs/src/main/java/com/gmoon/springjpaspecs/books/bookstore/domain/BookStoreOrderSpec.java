package com.gmoon.springjpaspecs.books.bookstore.domain;

import static com.gmoon.springjpaspecs.books.book.domain.QBook.*;
import static com.gmoon.springjpaspecs.books.bookstore.domain.QBookStoreBook.*;

import com.gmoon.springjpaspecs.books.bookstore.model.SortTargetType;
import com.gmoon.springjpaspecs.global.specs.orderby.OrderSpecification;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookStoreOrderSpec implements OrderSpecification {

	private Order order;
	private Expression target;

	public static BookStoreOrderSpec create(Order order, SortTargetType sortTargetType) {
		BookStoreOrderSpec spec = new BookStoreOrderSpec();
		spec.target = spec.getSortTargetColumnExpression(sortTargetType);
		spec.order = order;
		return spec;
	}

	@Override
	public void orderBy(JPAQuery<?> query) {
		query.orderBy(new OrderSpecifier<>(order, target));
	}

	private Expression getSortTargetColumnExpression(SortTargetType sortTargetType) {
		EntityPathBase<?> qDomain = getQDomainPath(sortTargetType);
		PathMetadata metadata = qDomain.getMetadata();
		Class<?> entityClass = qDomain.getType();
		String qDomainPathName = metadata.getName();

		return new PathBuilder<>(
			entityClass,
			String.format("%s.%s", qDomainPathName, sortTargetType.getValue())
		);
	}

	private EntityPathBase<?> getQDomainPath(SortTargetType type) {
		if (type == SortTargetType.PRICE
			|| type == SortTargetType.NAME) {
			return book;
		}

		if (type == SortTargetType.DATE) {
			return bookStoreBook;
		}
		throw new IllegalArgumentException();
	}
}
