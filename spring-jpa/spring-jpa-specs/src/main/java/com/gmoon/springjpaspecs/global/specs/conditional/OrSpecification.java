package com.gmoon.springjpaspecs.global.specs.conditional;

import java.util.Arrays;
import java.util.List;

class OrSpecification<T> implements Specification<T> {

	private final List<Specification<T>> values;

	protected OrSpecification(Specification<T>... specs) {
		values = Arrays.asList(specs);
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
