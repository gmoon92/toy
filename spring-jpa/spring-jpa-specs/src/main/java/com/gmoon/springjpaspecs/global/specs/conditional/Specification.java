package com.gmoon.springjpaspecs.global.specs.conditional;

import com.querydsl.core.types.Predicate;

public interface Specification<T> {

	Predicate isSatisfiedBy(T root);
}
