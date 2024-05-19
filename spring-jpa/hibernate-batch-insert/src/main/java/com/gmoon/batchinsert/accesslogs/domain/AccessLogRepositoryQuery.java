package com.gmoon.batchinsert.accesslogs.domain;

import java.util.List;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.gmoon.batchinsert.global.jmodel.tables.TbAccessLog;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AccessLogRepositoryQuery {

	private static final TbAccessLog jAccessLog = TbAccessLog.TB_ACCESS_LOG;

	private final DSLContext dsl;

	public List<AccessLog> findAllByUsername(String username) {
		return dsl.select()
			 .from(jAccessLog)
			 .where(jAccessLog.USERNAME.eq(username))
			 .fetchInto(AccessLog.class);
	}
}
