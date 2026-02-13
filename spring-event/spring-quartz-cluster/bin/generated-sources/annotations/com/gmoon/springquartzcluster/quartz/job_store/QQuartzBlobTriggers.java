package com.gmoon.springquartzcluster.quartz.job_store;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QQuartzBlobTriggers is a Querydsl query type for QuartzBlobTriggers
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuartzBlobTriggers extends EntityPathBase<QuartzBlobTriggers> {

    private static final long serialVersionUID = -1190562778L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QQuartzBlobTriggers quartzBlobTriggers = new QQuartzBlobTriggers("quartzBlobTriggers");

    public final ArrayPath<byte[], Byte> blobData = createArray("blobData", byte[].class);

    public final com.gmoon.springquartzcluster.quartz.job_store.id.QQuartzTriggerId id;

    public final QQuartzTriggers triggers;

    public QQuartzBlobTriggers(String variable) {
        this(QuartzBlobTriggers.class, forVariable(variable), INITS);
    }

    public QQuartzBlobTriggers(Path<? extends QuartzBlobTriggers> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QQuartzBlobTriggers(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QQuartzBlobTriggers(PathMetadata metadata, PathInits inits) {
        this(QuartzBlobTriggers.class, metadata, inits);
    }

    public QQuartzBlobTriggers(Class<? extends QuartzBlobTriggers> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.id = inits.isInitialized("id") ? new com.gmoon.springquartzcluster.quartz.job_store.id.QQuartzTriggerId(forProperty("id")) : null;
        this.triggers = inits.isInitialized("triggers") ? new QQuartzTriggers(forProperty("triggers"), inits.get("triggers")) : null;
    }

}

