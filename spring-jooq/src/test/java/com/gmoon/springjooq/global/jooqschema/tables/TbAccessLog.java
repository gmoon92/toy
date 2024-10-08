/*
 * This file is generated by jOOQ.
 */
package com.gmoon.springjooq.global.jooqschema.tables;

import java.time.LocalDateTime;
import java.util.Collection;

import javax.annotation.processing.Generated;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.PlainSQL;
import org.jooq.QueryPart;
import org.jooq.SQL;
import org.jooq.Schema;
import org.jooq.Select;
import org.jooq.Stringly;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import com.gmoon.springjooq.global.jooqschema.Jooq;
import com.gmoon.springjooq.global.jooqschema.Keys;
import com.gmoon.springjooq.global.jooqschema.enums.TbAccessLogOs;
import com.gmoon.springjooq.global.jooqschema.tables.records.TbAccessLogRecord;

/**
 * This class is generated by jOOQ.
 */
@Generated(
	 value = {
		  "https://www.jooq.org",
		  "jOOQ version:3.19.8",
		  "schema version:V1"
	 },
	 comments = "This class is generated by jOOQ"
)
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class TbAccessLog extends TableImpl<TbAccessLogRecord> {

	/**
	 * The reference instance of <code>jooq.tb_access_log</code>
	 */
	public static final TbAccessLog TB_ACCESS_LOG = new TbAccessLog();
	private static final long serialVersionUID = 1L;
	/**
	 * The column <code>jooq.tb_access_log.attempt_at</code>.
	 */
	public final TableField<TbAccessLogRecord, LocalDateTime> ATTEMPT_AT = createField(DSL.name("attempt_at"),
		 SQLDataType.LOCALDATETIME(6).defaultValue(DSL.inline("NULL", SQLDataType.LOCALDATETIME)), this, "");
	/**
	 * The column <code>jooq.tb_access_log.username</code>.
	 */
	public final TableField<TbAccessLogRecord, String> USERNAME = createField(DSL.name("username"),
		 SQLDataType.VARCHAR(50).defaultValue(DSL.inline("NULL", SQLDataType.VARCHAR)), this, "");

	/**
	 * The column <code>jooq.tb_access_log.id</code>.
	 */
	public final TableField<TbAccessLogRecord, String> ID = createField(DSL.name("id"),
		 SQLDataType.VARCHAR(50).nullable(false), this, "");
	/**
	 * The column <code>jooq.tb_access_log.ip</code>.
	 */
	public final TableField<TbAccessLogRecord, String> IP = createField(DSL.name("ip"),
		 SQLDataType.VARCHAR(100).defaultValue(DSL.inline("NULL", SQLDataType.VARCHAR)), this, "");
	/**
	 * The column <code>jooq.tb_access_log.os</code>.
	 */
	public final TableField<TbAccessLogRecord, TbAccessLogOs> OS = createField(DSL.name("os"), SQLDataType.VARCHAR(7)
		 .defaultValue(DSL.inline("NULL", SQLDataType.VARCHAR))
		 .asEnumDataType(TbAccessLogOs.class), this, "");

	private TbAccessLog(Name alias, Table<TbAccessLogRecord> aliased) {
		this(alias, aliased, (Field<?>[])null, null);
	}

	private TbAccessLog(Name alias, Table<TbAccessLogRecord> aliased, Field<?>[] parameters, Condition where) {
		super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
	}

	/**
	 * Create an aliased <code>jooq.tb_access_log</code> table reference
	 */
	public TbAccessLog(String alias) {
		this(DSL.name(alias), TB_ACCESS_LOG);
	}

	/**
	 * Create an aliased <code>jooq.tb_access_log</code> table reference
	 */
	public TbAccessLog(Name alias) {
		this(alias, TB_ACCESS_LOG);
	}

	/**
	 * Create a <code>jooq.tb_access_log</code> table reference
	 */
	public TbAccessLog() {
		this(DSL.name("tb_access_log"), null);
	}

	/**
	 * The class holding records for this type
	 */
	@Override
	public Class<TbAccessLogRecord> getRecordType() {
		return TbAccessLogRecord.class;
	}

	@Override
	public Schema getSchema() {
		return aliased() ? null : Jooq.JOOQ;
	}

	@Override
	public UniqueKey<TbAccessLogRecord> getPrimaryKey() {
		return Keys.KEY_TB_ACCESS_LOG_PRIMARY;
	}

	@Override
	public TbAccessLog as(String alias) {
		return new TbAccessLog(DSL.name(alias), this);
	}

	@Override
	public TbAccessLog as(Name alias) {
		return new TbAccessLog(alias, this);
	}

	@Override
	public TbAccessLog as(Table<?> alias) {
		return new TbAccessLog(alias.getQualifiedName(), this);
	}

	/**
	 * Rename this table
	 */
	@Override
	public TbAccessLog rename(String name) {
		return new TbAccessLog(DSL.name(name), null);
	}

	/**
	 * Rename this table
	 */
	@Override
	public TbAccessLog rename(Name name) {
		return new TbAccessLog(name, null);
	}

	/**
	 * Rename this table
	 */
	@Override
	public TbAccessLog rename(Table<?> name) {
		return new TbAccessLog(name.getQualifiedName(), null);
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	public TbAccessLog where(Condition condition) {
		return new TbAccessLog(getQualifiedName(), aliased() ? this : null, null, condition);
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	public TbAccessLog where(Collection<? extends Condition> conditions) {
		return where(DSL.and(conditions));
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	public TbAccessLog where(Condition... conditions) {
		return where(DSL.and(conditions));
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	public TbAccessLog where(Field<Boolean> condition) {
		return where(DSL.condition(condition));
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	@PlainSQL
	public TbAccessLog where(SQL condition) {
		return where(DSL.condition(condition));
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	@PlainSQL
	public TbAccessLog where(@Stringly.SQL String condition) {
		return where(DSL.condition(condition));
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	@PlainSQL
	public TbAccessLog where(@Stringly.SQL String condition, Object... binds) {
		return where(DSL.condition(condition, binds));
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	@PlainSQL
	public TbAccessLog where(@Stringly.SQL String condition, QueryPart... parts) {
		return where(DSL.condition(condition, parts));
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	public TbAccessLog whereExists(Select<?> select) {
		return where(DSL.exists(select));
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	public TbAccessLog whereNotExists(Select<?> select) {
		return where(DSL.notExists(select));
	}
}
