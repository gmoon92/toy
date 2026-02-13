package com.gmoon.batchinsert.coupons.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCouponGroup is a Querydsl query type for CouponGroup
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCouponGroup extends EntityPathBase<CouponGroup> {

    private static final long serialVersionUID = -160100706L;

    public static final QCouponGroup couponGroup = new QCouponGroup("couponGroup");

    public final NumberPath<Integer> couponCount = createNumber("couponCount", Integer.class);

    public final DateTimePath<java.time.Instant> createdAt = createDateTime("createdAt", java.time.Instant.class);

    public final DateTimePath<java.time.Instant> endAt = createDateTime("endAt", java.time.Instant.class);

    public final StringPath id = createString("id");

    public final StringPath name = createString("name");

    public final DateTimePath<java.time.Instant> startAt = createDateTime("startAt", java.time.Instant.class);

    public QCouponGroup(String variable) {
        super(CouponGroup.class, forVariable(variable));
    }

    public QCouponGroup(Path<? extends CouponGroup> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCouponGroup(PathMetadata metadata) {
        super(CouponGroup.class, metadata);
    }

}

