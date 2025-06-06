/*
 * This file is generated by jOOQ.
 */
package com.gmoon.batchinsert.global.jmodel.tables;


import com.gmoon.batchinsert.global.jmodel.Batchinsert;
import com.gmoon.batchinsert.global.jmodel.Keys;
import com.gmoon.batchinsert.global.jmodel.tables.TbCouponGroup.TbCouponGroupPath;
import com.gmoon.batchinsert.global.jmodel.tables.records.TbCouponRecord;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import javax.annotation.processing.Generated;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
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
public class TbCoupon extends TableImpl<TbCouponRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>batchinsert.tb_coupon</code>
     */
    public static final TbCoupon TB_COUPON = new TbCoupon();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<TbCouponRecord> getRecordType() {
        return TbCouponRecord.class;
    }

    /**
     * The column <code>batchinsert.tb_coupon.no</code>.
     */
    public final TableField<TbCouponRecord, String> NO = createField(DSL.name("no"), SQLDataType.VARCHAR(50).nullable(false), this, "");

    /**
     * The column <code>batchinsert.tb_coupon.coupon_group_id</code>.
     */
    public final TableField<TbCouponRecord, String> COUPON_GROUP_ID = createField(DSL.name("coupon_group_id"), SQLDataType.VARCHAR(50).nullable(false), this, "");

    /**
     * The column <code>batchinsert.tb_coupon.start_at</code>.
     */
    public final TableField<TbCouponRecord, LocalDateTime> START_AT = createField(DSL.name("start_at"), SQLDataType.LOCALDATETIME(0).nullable(false), this, "");

    /**
     * The column <code>batchinsert.tb_coupon.end_at</code>.
     */
    public final TableField<TbCouponRecord, LocalDateTime> END_AT = createField(DSL.name("end_at"), SQLDataType.LOCALDATETIME(0).nullable(false), this, "");

    /**
     * The column <code>batchinsert.tb_coupon.created_at</code>.
     */
    public final TableField<TbCouponRecord, LocalDateTime> CREATED_AT = createField(DSL.name("created_at"), SQLDataType.LOCALDATETIME(0).nullable(false), this, "");

    private TbCoupon(Name alias, Table<TbCouponRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private TbCoupon(Name alias, Table<TbCouponRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>batchinsert.tb_coupon</code> table reference
     */
    public TbCoupon(String alias) {
        this(DSL.name(alias), TB_COUPON);
    }

    /**
     * Create an aliased <code>batchinsert.tb_coupon</code> table reference
     */
    public TbCoupon(Name alias) {
        this(alias, TB_COUPON);
    }

    /**
     * Create a <code>batchinsert.tb_coupon</code> table reference
     */
    public TbCoupon() {
        this(DSL.name("tb_coupon"), null);
    }

    public <O extends Record> TbCoupon(Table<O> path, ForeignKey<O, TbCouponRecord> childPath, InverseForeignKey<O, TbCouponRecord> parentPath) {
        super(path, childPath, parentPath, TB_COUPON);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    @Generated(
        value = {
            "https://www.jooq.org",
            "jOOQ version:3.19.21",
            "schema version:4"
        },
        comments = "This class is generated by jOOQ"
    )
    public static class TbCouponPath extends TbCoupon implements Path<TbCouponRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> TbCouponPath(Table<O> path, ForeignKey<O, TbCouponRecord> childPath, InverseForeignKey<O, TbCouponRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private TbCouponPath(Name alias, Table<TbCouponRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public TbCouponPath as(String alias) {
            return new TbCouponPath(DSL.name(alias), this);
        }

        @Override
        public TbCouponPath as(Name alias) {
            return new TbCouponPath(alias, this);
        }

        @Override
        public TbCouponPath as(Table<?> alias) {
            return new TbCouponPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Batchinsert.BATCHINSERT;
    }

    @Override
    public UniqueKey<TbCouponRecord> getPrimaryKey() {
        return Keys.KEY_TB_COUPON_PRIMARY;
    }

    @Override
    public List<ForeignKey<TbCouponRecord, ?>> getReferences() {
        return Arrays.asList(Keys.FK_COUPON_COUPON_GROUP_ID);
    }

    private transient TbCouponGroupPath _tbCouponGroup;

    /**
     * Get the implicit join path to the
     * <code>batchinsert.tb_coupon_group</code> table.
     */
    public TbCouponGroupPath tbCouponGroup() {
        if (_tbCouponGroup == null)
            _tbCouponGroup = new TbCouponGroupPath(this, Keys.FK_COUPON_COUPON_GROUP_ID, null);

        return _tbCouponGroup;
    }

    @Override
    public TbCoupon as(String alias) {
        return new TbCoupon(DSL.name(alias), this);
    }

    @Override
    public TbCoupon as(Name alias) {
        return new TbCoupon(alias, this);
    }

    @Override
    public TbCoupon as(Table<?> alias) {
        return new TbCoupon(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public TbCoupon rename(String name) {
        return new TbCoupon(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public TbCoupon rename(Name name) {
        return new TbCoupon(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public TbCoupon rename(Table<?> name) {
        return new TbCoupon(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public TbCoupon where(Condition condition) {
        return new TbCoupon(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public TbCoupon where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public TbCoupon where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public TbCoupon where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public TbCoupon where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public TbCoupon where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public TbCoupon where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public TbCoupon where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public TbCoupon whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public TbCoupon whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
