package com.gmoon.springjpapagination.global.domain;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.gmoon.javacore.util.ReflectionUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UnionPagination implements CreatePagination {

	private static final BiFunction<Class<? extends BasePageable>, Supplier<Long>, BasePageable> PROVIDER;

	static {
		PROVIDER = (clazz, executeCountQuery) -> {
			BasePageable paginatedContent = ReflectionUtils.newInstance(clazz);
			paginatedContent.setTotalCount(executeCountQuery.get());
			return paginatedContent;
		};
	}

	private final Class<? extends BasePageable> pageableClass;
	private final Supplier<Long> executeCountQuery;

	public static UnionPagination create(
		 Class<? extends BasePageable> pageableClass,
		 Supplier<Long> executeCountQuery
	) {
		return new UnionPagination(pageableClass, executeCountQuery);
	}

	@Override
	public BasePageable newPagination() {
		return PROVIDER.apply(pageableClass, executeCountQuery);
	}
}
