package com.gmoon.springschedulingquartz.server;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static com.gmoon.springschedulingquartz.server.QServer.server;

@RequiredArgsConstructor
public class ServerRepositoryImpl implements ServerRepositoryQueryDsl {
  private final JPAQueryFactory jpaQueryFactory;

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
