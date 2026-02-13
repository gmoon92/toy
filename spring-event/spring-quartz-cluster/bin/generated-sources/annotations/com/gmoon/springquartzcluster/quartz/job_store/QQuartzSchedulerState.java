package com.gmoon.springquartzcluster.quartz.job_store;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QQuartzSchedulerState is a Querydsl query type for QuartzSchedulerState
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuartzSchedulerState extends EntityPathBase<QuartzSchedulerState> {

    private static final long serialVersionUID = -1552774972L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QQuartzSchedulerState quartzSchedulerState = new QQuartzSchedulerState("quartzSchedulerState");

    public final NumberPath<Long> checkinInterval = createNumber("checkinInterval", Long.class);

    public final QQuartzSchedulerState_Id id;

    public final NumberPath<Long> lastCheckinTime = createNumber("lastCheckinTime", Long.class);

    public QQuartzSchedulerState(String variable) {
        this(QuartzSchedulerState.class, forVariable(variable), INITS);
    }

    public QQuartzSchedulerState(Path<? extends QuartzSchedulerState> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QQuartzSchedulerState(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QQuartzSchedulerState(PathMetadata metadata, PathInits inits) {
        this(QuartzSchedulerState.class, metadata, inits);
    }

    public QQuartzSchedulerState(Class<? extends QuartzSchedulerState> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.id = inits.isInitialized("id") ? new QQuartzSchedulerState_Id(forProperty("id")) : null;
    }

}

