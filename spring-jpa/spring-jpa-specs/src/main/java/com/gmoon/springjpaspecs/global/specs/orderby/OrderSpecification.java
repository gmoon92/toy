package com.gmoon.springjpaspecs.global.specs.orderby;

import com.querydsl.jpa.impl.JPAQuery;

public interface OrderSpecification {

	void orderBy(JPAQuery<?> query);
}
