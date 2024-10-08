/*
 * This file is generated by jOOQ.
 */
package com.gmoon.springjooq.global.jooqschema;

import java.util.Arrays;
import java.util.List;

import javax.annotation.processing.Generated;

import org.jooq.Catalog;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;

import com.gmoon.springjooq.global.jooqschema.tables.TbAccessLog;

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
public class Jooq extends SchemaImpl {

	/**
	 * The reference instance of <code>jooq</code>
	 */
	public static final Jooq JOOQ = new Jooq();
	private static final long serialVersionUID = 1L;
	/**
	 * The table <code>jooq.tb_access_log</code>.
	 */
	public final TbAccessLog TB_ACCESS_LOG = TbAccessLog.TB_ACCESS_LOG;

	/**
	 * No further instances allowed
	 */
	private Jooq() {
		super("jooq", null);
	}

	@Override
	public Catalog getCatalog() {
		return DefaultCatalog.DEFAULT_CATALOG;
	}

	@Override
	public final List<Table<?>> getTables() {
		return Arrays.asList(
			 TbAccessLog.TB_ACCESS_LOG
		);
	}
}
