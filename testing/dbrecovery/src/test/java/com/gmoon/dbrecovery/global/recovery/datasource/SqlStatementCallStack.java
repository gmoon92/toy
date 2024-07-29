package com.gmoon.dbrecovery.global.recovery.datasource;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.statement.Statement;

import java.util.EnumSet;
import java.util.List;
import java.util.Stack;

@Slf4j
@ToString
public class SqlStatementCallStack {

	@Getter
	private final Stack<String> value = new Stack<>();
	private final EnumSet<DmlStatement> includes;

	public SqlStatementCallStack(DmlStatement... includes) {
		this.includes = EnumSet.copyOf(List.of(includes));
	}

	public void push(String sql) {
		if (isAllowedSqlStatement(sql)) {
			value.push(sql);
		}
	}

	private boolean isAllowedSqlStatement(String sql) {
		Statement statement = SqlParser.getStatement(sql);
		return includes.contains(DmlStatement.from(statement));
	}


	public void clear() {
		value.clear();
	}
}
