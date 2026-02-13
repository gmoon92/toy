package com.gmoon.springquartzcluster.quartz;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QQuartzSchedulerHistoryDetail is a Querydsl query type for QuartzSchedulerHistoryDetail
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuartzSchedulerHistoryDetail extends EntityPathBase<QuartzSchedulerHistoryDetail> {

    private static final long serialVersionUID = -811098487L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QQuartzSchedulerHistoryDetail quartzSchedulerHistoryDetail = new QQuartzSchedulerHistoryDetail("quartzSchedulerHistoryDetail");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath ip = createString("ip");

    public final QQuartzSchedulerHistory schedulerHistory;

    public QQuartzSchedulerHistoryDetail(String variable) {
        this(QuartzSchedulerHistoryDetail.class, forVariable(variable), INITS);
    }

    public QQuartzSchedulerHistoryDetail(Path<? extends QuartzSchedulerHistoryDetail> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QQuartzSchedulerHistoryDetail(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QQuartzSchedulerHistoryDetail(PathMetadata metadata, PathInits inits) {
        this(QuartzSchedulerHistoryDetail.class, metadata, inits);
    }

    public QQuartzSchedulerHistoryDetail(Class<? extends QuartzSchedulerHistoryDetail> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.schedulerHistory = inits.isInitialized("schedulerHistory") ? new QQuartzSchedulerHistory(forProperty("schedulerHistory")) : null;
    }

}

