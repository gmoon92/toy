package com.gmoon.springquartzcluster.quartz.job_store;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QQuartzCronTriggers is a Querydsl query type for QuartzCronTriggers
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuartzCronTriggers extends EntityPathBase<QuartzCronTriggers> {

    private static final long serialVersionUID = 827096599L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QQuartzCronTriggers quartzCronTriggers = new QQuartzCronTriggers("quartzCronTriggers");

    public final StringPath cronExpression = createString("cronExpression");

    public final com.gmoon.springquartzcluster.quartz.job_store.id.QQuartzTriggerId id;

    public final StringPath timeZoneId = createString("timeZoneId");

    public final QQuartzTriggers triggers;

    public QQuartzCronTriggers(String variable) {
        this(QuartzCronTriggers.class, forVariable(variable), INITS);
    }

    public QQuartzCronTriggers(Path<? extends QuartzCronTriggers> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QQuartzCronTriggers(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QQuartzCronTriggers(PathMetadata metadata, PathInits inits) {
        this(QuartzCronTriggers.class, metadata, inits);
    }

    public QQuartzCronTriggers(Class<? extends QuartzCronTriggers> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.id = inits.isInitialized("id") ? new com.gmoon.springquartzcluster.quartz.job_store.id.QQuartzTriggerId(forProperty("id")) : null;
        this.triggers = inits.isInitialized("triggers") ? new QQuartzTriggers(forProperty("triggers"), inits.get("triggers")) : null;
    }

}

