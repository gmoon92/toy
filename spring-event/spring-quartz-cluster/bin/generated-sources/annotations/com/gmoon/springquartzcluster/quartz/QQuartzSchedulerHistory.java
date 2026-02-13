package com.gmoon.springquartzcluster.quartz;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QQuartzSchedulerHistory is a Querydsl query type for QuartzSchedulerHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuartzSchedulerHistory extends EntityPathBase<QuartzSchedulerHistory> {

    private static final long serialVersionUID = 1652296728L;

    public static final QQuartzSchedulerHistory quartzSchedulerHistory = new QQuartzSchedulerHistory("quartzSchedulerHistory");

    public final ListPath<QuartzSchedulerHistoryDetail, QQuartzSchedulerHistoryDetail> details = this.<QuartzSchedulerHistoryDetail, QQuartzSchedulerHistoryDetail>createList("details", QuartzSchedulerHistoryDetail.class, QQuartzSchedulerHistoryDetail.class, PathInits.DIRECT2);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath instanceId = createString("instanceId");

    public QQuartzSchedulerHistory(String variable) {
        super(QuartzSchedulerHistory.class, forVariable(variable));
    }

    public QQuartzSchedulerHistory(Path<? extends QuartzSchedulerHistory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QQuartzSchedulerHistory(PathMetadata metadata) {
        super(QuartzSchedulerHistory.class, metadata);
    }

}

