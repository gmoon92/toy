package com.gmoon.springjpaspecs.global.specs.conditional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
class NotSpecification<T> implements Specification<T> {

	private final Specification<T> value;

	@Override
	public boolean isSatisfiedBy(T root) {
		return !value.isSatisfiedBy(root);
	}
}
