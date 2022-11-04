package com.gmoon.springjpaspecs.global.specs.orderby;

import java.util.ArrayList;
import java.util.List;

import com.querydsl.jpa.impl.JPAQuery;

public class OrderSpecs implements OrderSpecification {

	private final List<OrderSpecification> values = new ArrayList<>();

	@Override
	public void orderBy(JPAQuery<?> query) {
		for (OrderSpecification spec : values) {
			spec.orderBy(query);
		}
	}

	public void add(OrderSpecification spec) {
		values.add(spec);
	}
}
