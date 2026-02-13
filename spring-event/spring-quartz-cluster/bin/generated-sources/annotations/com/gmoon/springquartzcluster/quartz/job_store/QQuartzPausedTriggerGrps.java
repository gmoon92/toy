package com.gmoon.springquartzcluster.quartz.job_store;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QQuartzPausedTriggerGrps is a Querydsl query type for QuartzPausedTriggerGrps
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuartzPausedTriggerGrps extends EntityPathBase<QuartzPausedTriggerGrps> {

    private static final long serialVersionUID = -557281014L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QQuartzPausedTriggerGrps quartzPausedTriggerGrps = new QQuartzPausedTriggerGrps("quartzPausedTriggerGrps");

    public final QQuartzPausedTriggerGrps_Id id;

    public QQuartzPausedTriggerGrps(String variable) {
        this(QuartzPausedTriggerGrps.class, forVariable(variable), INITS);
    }

    public QQuartzPausedTriggerGrps(Path<? extends QuartzPausedTriggerGrps> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QQuartzPausedTriggerGrps(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QQuartzPausedTriggerGrps(PathMetadata metadata, PathInits inits) {
        this(QuartzPausedTriggerGrps.class, metadata, inits);
    }

    public QQuartzPausedTriggerGrps(Class<? extends QuartzPausedTriggerGrps> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.id = inits.isInitialized("id") ? new QQuartzPausedTriggerGrps_Id(forProperty("id")) : null;
    }

}

