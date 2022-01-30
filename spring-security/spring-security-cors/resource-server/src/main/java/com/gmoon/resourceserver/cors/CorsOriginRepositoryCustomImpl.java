package com.gmoon.resourceserver.cors;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CorsOriginRepositoryCustomImpl implements CorsOriginRepositoryCustom {
	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public List<String> getAllHost() {
		QCorsOrigin corsOrigin = QCorsOrigin.corsOrigin;
		return jpaQueryFactory.select(corsOrigin.origin.host)
			.from(corsOrigin)
			.groupBy(corsOrigin.origin.host)
			.fetch();
	}
}
