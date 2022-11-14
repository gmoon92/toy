package com.gmoon.springjpaspecs.global.specs.conditional;

import java.util.ArrayList;
import java.util.List;

public final class Specs<T> implements Specification<T> {

	private final List<Specification<T>> values = new ArrayList<>();

	public static <T> Specs<T> create() {
		return new Specs<>();
	}

	public Specs<T> or(Specification<T>... specs) {
		values.add(new OrSpecification(specs));
		return this;
	}

	public Specs<T> and(Specification<T>... specs) {
		values.add(new AndSpecification(specs));
		return this;
	}

	public Specs<T> not(Specification<T> spec) {
		values.add(new NotSpecification(spec));
		return this;
	}

	@Override
	public boolean isSatisfiedBy(T root) {
		for (Specification<T> spec : values) {
			if (spec.isSatisfiedBy(root)) {
				return true;
			}
		}

		return false;
	}
}
