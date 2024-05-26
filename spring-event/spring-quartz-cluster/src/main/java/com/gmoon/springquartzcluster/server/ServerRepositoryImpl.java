package com.gmoon.springquartzcluster.server;

import static com.gmoon.springquartzcluster.server.QServer.*;

import java.util.List;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import com.gmoon.javacore.util.StringUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ServerRepositoryImpl implements ServerRepositoryQueryDsl {
	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public List<Server> getEnabledServers() {
		return jpaQueryFactory.select(server)
			 .from(server)
			 .where(server.isEnabled())
			 .fetch();
	}

	@Override
	public List<Server> getServers(String serverName) {
		return jpaQueryFactory.select(server)
			 .from(server)
			 .where(eqServerName(serverName))
			 .fetch();
	}

	private BooleanExpression eqServerName(String serverName) {
		if (StringUtils.isBlank(serverName)) {
			return null;
		}
		return server.name.eq(serverName);
	}
}
