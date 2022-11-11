package com.gmoon.springjpaspecs.global.specs.conditional;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class NotSpecification<T> extends CompositeSpecification<T> {

	private final Specification<T> spec;

	public static <T> NotSpecification<T> of(Specification<T> spec) {
		return new NotSpecification<>(spec);
	}

	@Override
	public Predicate isSatisfiedBy(T root) {
		return new BooleanBuilder(spec.isSatisfiedBy(root))
			.not();
	}
}
