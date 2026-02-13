package com.gmoon.springquartzcluster.quartz.job_store;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QQuartzFiredTriggers is a Querydsl query type for QuartzFiredTriggers
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuartzFiredTriggers extends EntityPathBase<QuartzFiredTriggers> {

    private static final long serialVersionUID = -1643326757L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QQuartzFiredTriggers quartzFiredTriggers = new QQuartzFiredTriggers("quartzFiredTriggers");

    public final NumberPath<Long> firedTime = createNumber("firedTime", Long.class);

    public final QQuartzFiredTriggers_Id id;

    public final StringPath instanceName = createString("instanceName");

    public final StringPath jobGroup = createString("jobGroup");

    public final StringPath jobName = createString("jobName");

    public final NumberPath<Integer> priority = createNumber("priority", Integer.class);

    public final NumberPath<Long> schedulerTime = createNumber("schedulerTime", Long.class);

    public final StringPath state = createString("state");

    public final StringPath triggerGroup = createString("triggerGroup");

    public final StringPath triggerName = createString("triggerName");

    public final StringPath useNonConcurrent = createString("useNonConcurrent");

    public final StringPath useRequestsRecovery = createString("useRequestsRecovery");

    public QQuartzFiredTriggers(String variable) {
        this(QuartzFiredTriggers.class, forVariable(variable), INITS);
    }

    public QQuartzFiredTriggers(Path<? extends QuartzFiredTriggers> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QQuartzFiredTriggers(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QQuartzFiredTriggers(PathMetadata metadata, PathInits inits) {
        this(QuartzFiredTriggers.class, metadata, inits);
    }

    public QQuartzFiredTriggers(Class<? extends QuartzFiredTriggers> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.id = inits.isInitialized("id") ? new QQuartzFiredTriggers_Id(forProperty("id")) : null;
    }

}

