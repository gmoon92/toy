package com.gmoon.dbrestore.test.dbrestore.datasource;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SqlParser {


	/**
	 * ALTER TABLE DROP FOREIGN KEY statement unable to parse
	 * issued https://github.com/JSQLParser/JSqlParser/issues/990
	 */
	public static Statement getStatement(String sql) {
		try {
			CCJSqlParser parser = CCJSqlParserUtil.newParser(sql);
			return parser.Statement();
		} catch (Exception e) {
			log.trace("unable to parse sql: {}", sql);
			return null;
		}
	}

	public static Table getTable(Statement statement) {
		if (statement instanceof Delete delete) {
			return delete.getTable();
		} else if (statement instanceof Update update) {
			return update.getTable();
		} else if (statement instanceof Insert insert) {
			return insert.getTable();
		}
		throw new IllegalArgumentException("Unsupported sql statement.");
	}
}
