package com.gmoon.resourceserver.cors;

import static com.gmoon.resourceserver.cors.QCorsOrigin.*;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CorsOriginRepositoryCustomImpl implements CorsOriginRepositoryCustom {
	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public List<String> getAllHost() {
		return jpaQueryFactory.select(corsOrigin.origin.host)
			 .from(corsOrigin)
			 .groupBy(corsOrigin.origin.host)
			 .fetch();
	}
}
