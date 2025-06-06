/*
 * This file is generated by jOOQ.
 */
package com.gmoon.batchinsert.global.jmodel;


import com.gmoon.batchinsert.global.jmodel.tables.*;
import org.jooq.Catalog;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;

import javax.annotation.processing.Generated;
import java.util.Arrays;
import java.util.List;


/**
 * This class is generated by jOOQ.
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
public class Batchinsert extends SchemaImpl {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>batchinsert</code>
     */
    public static final Batchinsert BATCHINSERT = new Batchinsert();

    /**
     * The table <code>batchinsert.ex_access_log</code>.
     */
    public final ExAccessLog EX_ACCESS_LOG = ExAccessLog.EX_ACCESS_LOG;

    /**
     * The table <code>batchinsert.flyway_schema_history</code>.
     */
    public final FlywaySchemaHistory FLYWAY_SCHEMA_HISTORY = FlywaySchemaHistory.FLYWAY_SCHEMA_HISTORY;

    /**
     * The table <code>batchinsert.tb_access_log</code>.
     */
    public final TbAccessLog TB_ACCESS_LOG = TbAccessLog.TB_ACCESS_LOG;

    /**
     * The table <code>batchinsert.tb_coupon</code>.
     */
    public final TbCoupon TB_COUPON = TbCoupon.TB_COUPON;

    /**
     * The table <code>batchinsert.tb_coupon_group</code>.
     */
    public final TbCouponGroup TB_COUPON_GROUP = TbCouponGroup.TB_COUPON_GROUP;

    /**
     * No further instances allowed
     */
    private Batchinsert() {
        super("batchinsert", null);
    }


    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Table<?>> getTables() {
        return Arrays.asList(
            ExAccessLog.EX_ACCESS_LOG,
            FlywaySchemaHistory.FLYWAY_SCHEMA_HISTORY,
            TbAccessLog.TB_ACCESS_LOG,
            TbCoupon.TB_COUPON,
            TbCouponGroup.TB_COUPON_GROUP
        );
    }
}
