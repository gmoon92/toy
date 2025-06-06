/*
 * This file is generated by jOOQ.
 */
package com.gmoon.batchinsert.global.jmodel;


import com.gmoon.batchinsert.global.jmodel.tables.FlywaySchemaHistory;
import org.jooq.Index;
import org.jooq.OrderField;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;

import javax.annotation.processing.Generated;


/**
 * A class modelling indexes of tables in batchinsert.
 */
@Generated(
    value = {
        "https://www.jooq.org",
        "jOOQ version:3.19.21",
        "schema version:4"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Indexes {

    // -------------------------------------------------------------------------
    // INDEX definitions
    // -------------------------------------------------------------------------

    public static final Index FLYWAY_SCHEMA_HISTORY_FLYWAY_SCHEMA_HISTORY_S_IDX = Internal.createIndex(DSL.name("flyway_schema_history_s_idx"), FlywaySchemaHistory.FLYWAY_SCHEMA_HISTORY, new OrderField[] { FlywaySchemaHistory.FLYWAY_SCHEMA_HISTORY.SUCCESS }, false);
}
