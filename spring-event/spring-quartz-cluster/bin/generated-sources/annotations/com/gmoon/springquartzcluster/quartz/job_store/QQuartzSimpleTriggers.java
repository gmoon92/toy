package com.gmoon.springquartzcluster.quartz.job_store;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QQuartzSimpleTriggers is a Querydsl query type for QuartzSimpleTriggers
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuartzSimpleTriggers extends EntityPathBase<QuartzSimpleTriggers> {

    private static final long serialVersionUID = 153247163L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QQuartzSimpleTriggers quartzSimpleTriggers = new QQuartzSimpleTriggers("quartzSimpleTriggers");

    public final com.gmoon.springquartzcluster.quartz.job_store.id.QQuartzTriggerId id;

    public final NumberPath<Long> repeatCount = createNumber("repeatCount", Long.class);

    public final NumberPath<Long> repeatInterval = createNumber("repeatInterval", Long.class);

    public final NumberPath<Long> timesTriggered = createNumber("timesTriggered", Long.class);

    public final QQuartzTriggers triggers;

    public QQuartzSimpleTriggers(String variable) {
        this(QuartzSimpleTriggers.class, forVariable(variable), INITS);
    }

    public QQuartzSimpleTriggers(Path<? extends QuartzSimpleTriggers> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QQuartzSimpleTriggers(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QQuartzSimpleTriggers(PathMetadata metadata, PathInits inits) {
        this(QuartzSimpleTriggers.class, metadata, inits);
    }

    public QQuartzSimpleTriggers(Class<? extends QuartzSimpleTriggers> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.id = inits.isInitialized("id") ? new com.gmoon.springquartzcluster.quartz.job_store.id.QQuartzTriggerId(forProperty("id")) : null;
        this.triggers = inits.isInitialized("triggers") ? new QQuartzTriggers(forProperty("triggers"), inits.get("triggers")) : null;
    }

}

