package com.gmoon.springjpaspecs.global.specs.conditional;

public interface Specification<T> {

	boolean isSatisfiedBy(T root);
}
