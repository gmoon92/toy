package com.gmoon.dbrecovery.global.recovery.datasource;

import com.gmoon.dbrecovery.global.recovery.datasource.vo.KeyValue;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
		if (statement instanceof Delete) {
			return ( (Delete) statement ).getTable();
		} else if (statement instanceof Update) {
			return ( (Update) statement ).getTable();
		} else if (statement instanceof Insert) {
			return ( (Insert) statement ).getTable();
		}
		throw new IllegalArgumentException("Not detected statement.");
	}

	public static String getTableName(String sql) {
		Statement statement = getStatement(sql);
		if (statement == null) {
			return "";
		}

		Table table = getTable(statement);
		return table.getName();
	}

	public static String getUpdateRecoverySql(String schema, String backupSchema, String sql, Set<KeyValue> keyValues) {
		Update update = (Update) getStatement(sql);
		Table table = update.getTable();
		String tableName = table.getName();

		Table schemaTable = new Table(schema, tableName);
		Table backupTable = new Table(backupSchema, tableName);

		Update recoveryUpdate = new Update();
		recoveryUpdate.setTable(schemaTable);
		recoveryUpdate.setStartJoins(Collections.singletonList(getJoin(schemaTable, backupTable, keyValues)));
		recoveryUpdate.setUpdateSets(generateSetClause(update, schemaTable, backupTable));
		recoveryUpdate.setWhere(generateWhereClause(update.getWhere(), schemaTable));
		return recoveryUpdate.toString();
	}

	private static Join getJoin(Table schemaTable, Table backupTable, Set<KeyValue> keyValues) {
		Join innerJoin = new Join();
		innerJoin.setInner(true);
		innerJoin.setFromItem(backupTable);

		Expression onExpression = null;
		for (KeyValue keyValue : keyValues) {
			String keyColumnName = keyValue.getKeyColumnName();
			EqualsTo equalsTo = new EqualsTo();
			equalsTo.setLeftExpression(new Column(schemaTable, keyColumnName));
			equalsTo.setRightExpression(new Column(backupTable, keyColumnName));
			if (onExpression == null) {
				onExpression = equalsTo;
			} else {
				onExpression = new AndExpression(onExpression, equalsTo);
			}
		}

		innerJoin.addOnExpression(onExpression);
		return innerJoin;
	}

	private static List<UpdateSet> generateSetClause(Update update, Table schemaTable, Table backupTable) {
		return update.getUpdateSets().stream()
			 .flatMap(updateSet -> updateSet.getColumns().stream())
			 .map(column -> {
				 Column schemaColumn = new Column(schemaTable, column.getColumnName());
				 Column backupColumn = new Column(backupTable, column.getColumnName());
				 return new UpdateSet(schemaColumn, backupColumn);
			 })
			 .collect(Collectors.toList());
	}

	public static String getInsertRecoverySql(String sql) {
		Insert insert = (Insert) getStatement(sql);

		Expression where = null;
		ExpressionList<Column> columns = insert.getColumns();
		ExpressionList<?> values = insert.getValues().getExpressions();
		for (int i = 0; i < columns.size(); i++) {
			Column column = columns.get(i);
			Expression value = values.get(i);

			Expression condition;
			if (value instanceof NullValue) {
				IsNullExpression isNull = new IsNullExpression();
				isNull.setLeftExpression(column);
				condition = isNull;
			} else {
				EqualsTo equalsTo = new EqualsTo();
				equalsTo.setLeftExpression(column);
				equalsTo.setRightExpression(value);
				condition = equalsTo;
			}

			if (where == null) {
				where = condition;
			} else {
				where = new AndExpression(where, condition);
			}
		}

		if (where == null) {
			throw new RuntimeException("unable convert insert statement to recovery sql. " + sql);
		}

		Delete delete = new Delete();
		delete.setTable(insert.getTable());
		delete.setWhere(where);
		return delete.toString();
	}

	public static String getDeleteRecoverySql(String schema, String backupSchema, String sql, Set<KeyValue> keyValues) {
		Delete statement = (Delete) getStatement(sql);
		Table table = statement.getTable();
		table.setSchemaName(schema);
		String tableName = table.getName();

		Insert insert = new Insert();
		insert.setTable(table);

		Table backupTable = new Table(backupSchema, tableName);
		PlainSelect select = new PlainSelect();
		select.addSelectItems(new AllColumns());
		select.setFromItem(backupTable);
		select.setWhere(generateWhereClause(statement.getWhere(), backupTable));
		insert.setSelect(select);
		return insert.toString();
	}

	private static ComparisonOperator generateWhereClause(Expression where, Table table) {
		try {
			ComparisonOperator operator = (ComparisonOperator) where;
			String columnName = operator.getLeftExpression().toString();
			String columnFullName = String.format("%s.%s", table.getFullyQualifiedName(), columnName);
			operator.setLeftExpression(CCJSqlParserUtil.parseExpression(columnFullName));
			return operator;
		} catch (JSQLParserException e) {
			throw new RuntimeException(e);
		}
	}

	public static PlainSelect selectRecordForUpdate(String schema, String sql) {
		Update update = (Update) getStatement(sql);
		Table table = update.getTable();
		String tableName = table.getName();
		PlainSelect select = new PlainSelect();
		select.addSelectItems(new AllColumns());
		select.setFromItem(new Table(schema, tableName));
		select.setWhere(update.getWhere());
		return select;
	}
}
