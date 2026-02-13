package com.gmoon.springquartzcluster.quartz.job_store;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QQuartzLocks is a Querydsl query type for QuartzLocks
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuartzLocks extends EntityPathBase<QuartzLocks> {

    private static final long serialVersionUID = 1062149306L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QQuartzLocks quartzLocks = new QQuartzLocks("quartzLocks");

    public final QQuartzLocks_Id id;

    public QQuartzLocks(String variable) {
        this(QuartzLocks.class, forVariable(variable), INITS);
    }

    public QQuartzLocks(Path<? extends QuartzLocks> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QQuartzLocks(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QQuartzLocks(PathMetadata metadata, PathInits inits) {
        this(QuartzLocks.class, metadata, inits);
    }

    public QQuartzLocks(Class<? extends QuartzLocks> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.id = inits.isInitialized("id") ? new QQuartzLocks_Id(forProperty("id")) : null;
    }

}

