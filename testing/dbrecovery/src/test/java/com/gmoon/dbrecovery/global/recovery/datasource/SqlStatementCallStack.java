package com.gmoon.dbrecovery.global.recovery.datasource;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

import java.util.EnumSet;
import java.util.List;
import java.util.Stack;

@Slf4j
@ToString
public class SqlStatementCallStack {

	@Getter
	private final Stack<String> value = new Stack<>();
	private final EnumSet<DmlStatement> include;

	public SqlStatementCallStack(DmlStatement... include) {
		this.include = EnumSet.copyOf(List.of(include));
	}

	public void push(String sql) {
		if (isDmlStatement(sql)) {
			value.push(sql);
		}
	}

	private boolean isDmlStatement(String sql) {
		Statement statement = SqlParser.getStatement(sql);
		if (statement == null) {
			return false;
		}

		boolean isDmlStatement = statement instanceof Insert
			 || statement instanceof Select
			 || statement instanceof Update
			 || statement instanceof Delete;
		if (isDmlStatement) {
			return include.contains(DmlStatement.from(statement));
		}
		return false;
	}
}
