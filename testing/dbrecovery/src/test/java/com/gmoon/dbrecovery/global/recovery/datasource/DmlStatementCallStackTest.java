package com.gmoon.dbrecovery.global.recovery.datasource;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.ParseException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

class DmlStatementCallStackTest {

	/**
	 * 일부 DDL 미지원
	 * https://jsqlparser.github.io/JSqlParser/unsupported.html
	 */
	@Test
	void ddl() {
		String sql = "alter table tb_coupon drop foreign key FKe5km00l69rfrxvx5um3y1klw1";

		assertThrowsExactly(ParseException.class, () -> CCJSqlParserUtil.newParser(sql).Statement());
	}
}
