package com.gmoon.springjpapagination.global.domain;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.gmoon.javacore.util.ReflectionUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UnionPagination implements CreatePagination {

	private static final BiFunction<Class<? extends Pageable>, Supplier<Long>, Pageable> PROVIDER;

	static {
		PROVIDER = (clazz, executeCountQuery) -> {
			Pageable paginatedContent = ReflectionUtils.newInstance(clazz);
			paginatedContent.setTotalCount(executeCountQuery.get());
			return paginatedContent;
		};
	}

	private final Class<? extends Pageable> pageableClass;
	private final Supplier<Long> executeCountQuery;

	public static UnionPagination create(
		 Class<? extends Pageable> pageableClass,
		 Supplier<Long> executeCountQuery
	) {
		return new UnionPagination(pageableClass, executeCountQuery);
	}

	@Override
	public Pageable newPagination() {
		return PROVIDER.apply(pageableClass, executeCountQuery);
	}
}
