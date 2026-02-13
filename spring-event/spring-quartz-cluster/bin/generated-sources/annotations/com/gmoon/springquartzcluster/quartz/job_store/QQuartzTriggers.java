package com.gmoon.springquartzcluster.quartz.job_store;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QQuartzTriggers is a Querydsl query type for QuartzTriggers
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuartzTriggers extends EntityPathBase<QuartzTriggers> {

    private static final long serialVersionUID = 1058853993L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QQuartzTriggers quartzTriggers = new QQuartzTriggers("quartzTriggers");

    public final StringPath calendarName = createString("calendarName");

    public final StringPath description = createString("description");

    public final NumberPath<Long> endTime = createNumber("endTime", Long.class);

    public final com.gmoon.springquartzcluster.quartz.job_store.id.QQuartzTriggerId id;

    public final ArrayPath<byte[], Byte> jobData = createArray("jobData", byte[].class);

    public final QQuartzJobDetails jobDetails;

    public final StringPath jobGroup = createString("jobGroup");

    public final StringPath jobName = createString("jobName");

    public final NumberPath<Short> misfireInstr = createNumber("misfireInstr", Short.class);

    public final NumberPath<Long> nextFireTime = createNumber("nextFireTime", Long.class);

    public final NumberPath<Long> prevFireTime = createNumber("prevFireTime", Long.class);

    public final NumberPath<Integer> priority = createNumber("priority", Integer.class);

    public final NumberPath<Long> startTime = createNumber("startTime", Long.class);

    public final StringPath state = createString("state");

    public final StringPath type = createString("type");

    public QQuartzTriggers(String variable) {
        this(QuartzTriggers.class, forVariable(variable), INITS);
    }

    public QQuartzTriggers(Path<? extends QuartzTriggers> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QQuartzTriggers(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QQuartzTriggers(PathMetadata metadata, PathInits inits) {
        this(QuartzTriggers.class, metadata, inits);
    }

    public QQuartzTriggers(Class<? extends QuartzTriggers> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.id = inits.isInitialized("id") ? new com.gmoon.springquartzcluster.quartz.job_store.id.QQuartzTriggerId(forProperty("id")) : null;
        this.jobDetails = inits.isInitialized("jobDetails") ? new QQuartzJobDetails(forProperty("jobDetails"), inits.get("jobDetails")) : null;
    }

}

