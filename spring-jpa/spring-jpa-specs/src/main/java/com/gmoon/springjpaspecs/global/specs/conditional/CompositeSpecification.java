package com.gmoon.springjpaspecs.global.specs.conditional;

import java.util.ArrayList;
import java.util.List;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

public class CompositeSpecification<T> implements Specification<T> {

	private final List<Specification<T>> values = new ArrayList<>();

	public static <T> CompositeSpecification<T> create() {
		return new CompositeSpecification<>();
	}

	public CompositeSpecification<T> or(Specification<T>... specs) {
		values.add(OrSpecification.of(specs));
		return this;
	}

	public CompositeSpecification<T> and(Specification<T>... specs) {
		values.add(AndSpecification.of(specs));
		return this;
	}

	public CompositeSpecification<T> not(Specification<T> spec) {
		values.add(NotSpecification.of(spec));
		return this;
	}

	@Override
	public Predicate isSatisfiedBy(T root) {
		BooleanBuilder builder = new BooleanBuilder();
		for (Specification<T> spec : values) {
			if (spec instanceof AndSpecification
				|| spec instanceof NotSpecification
			) {
				builder.and(spec.isSatisfiedBy(root));
			}

			if (spec instanceof OrSpecification) {
				builder.orAllOf(spec.isSatisfiedBy(root));
			}
		}
		return builder;
	}
}
