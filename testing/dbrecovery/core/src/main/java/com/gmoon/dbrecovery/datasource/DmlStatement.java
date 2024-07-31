package com.gmoon.dbrecovery.datasource;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

public enum DmlStatement {

	INSERT, SELECT, UPDATE, DELETE, UNKNOWN;

	public static DmlStatement from(Statement statement) {
		if (statement == null) {
			return UNKNOWN;
		}

		if (statement instanceof Insert) {
			return INSERT;
		} else if (statement instanceof Select) {
			return SELECT;
		} else if (statement instanceof Update) {
			return UPDATE;
		} else if (statement instanceof Delete) {
			return DELETE;
		} else {
			return UNKNOWN;
		}
	}
}
