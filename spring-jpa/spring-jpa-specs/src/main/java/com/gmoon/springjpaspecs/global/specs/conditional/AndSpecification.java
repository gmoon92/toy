package com.gmoon.springjpaspecs.global.specs.conditional;

import java.util.Arrays;
import java.util.List;

class AndSpecification<T> implements Specification<T> {

	private final List<Specification<T>> values;

	protected AndSpecification(Specification<T>... specs) {
		values = Arrays.asList(specs);
	}

	@Override
	public boolean isSatisfiedBy(T root) {
		for (Specification<T> spec : values) {
			if (!spec.isSatisfiedBy(root)) {
				return false;
			}
		}

		return true;
	}
}

