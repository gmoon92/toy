package com.gmoon.springjpaspecs.global.specs.conditional;

import java.util.Arrays;
import java.util.List;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AndSpecification<T> extends CompositeSpecification<T> {

	private final List<Specification<T>> specs;

	public static <T> AndSpecification<T> of(Specification<T>... specs) {
		return new AndSpecification<>(Arrays.asList(specs));
	}

	@Override
	public Predicate isSatisfiedBy(T root) {
		return specs.stream()
			.map(spec -> new BooleanBuilder(spec.isSatisfiedBy(root)))
			.reduce(new BooleanBuilder(), BooleanBuilder::and);
	}
}
