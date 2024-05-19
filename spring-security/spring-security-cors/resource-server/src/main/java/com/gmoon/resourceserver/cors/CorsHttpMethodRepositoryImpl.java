package com.gmoon.resourceserver.cors;

import static com.gmoon.resourceserver.cors.QCorsHttpMethod.*;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CorsHttpMethodRepositoryImpl implements CorsHttpMethodRepositoryCustom {
	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public List<String> findAllByEnabled() {
		return jpaQueryFactory.select(corsHttpMethod.id.stringValue())
			 .from(corsHttpMethod)
			 .where(corsHttpMethod.enabled.isTrue())
			 .fetch();
	}
}
