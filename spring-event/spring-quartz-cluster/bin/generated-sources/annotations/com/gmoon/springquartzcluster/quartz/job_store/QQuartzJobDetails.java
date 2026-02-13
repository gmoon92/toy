package com.gmoon.springquartzcluster.quartz.job_store;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QQuartzJobDetails is a Querydsl query type for QuartzJobDetails
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuartzJobDetails extends EntityPathBase<QuartzJobDetails> {

    private static final long serialVersionUID = -1907544333L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QQuartzJobDetails quartzJobDetails = new QQuartzJobDetails("quartzJobDetails");

    public final StringPath description = createString("description");

    public final QQuartzJobDetails_Id id;

    public final StringPath JobClassName = createString("JobClassName");

    public final ArrayPath<byte[], Byte> jobData = createArray("jobData", byte[].class);

    public final StringPath useDurability = createString("useDurability");

    public final StringPath useNonConcurrent = createString("useNonConcurrent");

    public final StringPath useRequestsRecovery = createString("useRequestsRecovery");

    public final StringPath useUpdateData = createString("useUpdateData");

    public QQuartzJobDetails(String variable) {
        this(QuartzJobDetails.class, forVariable(variable), INITS);
    }

    public QQuartzJobDetails(Path<? extends QuartzJobDetails> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QQuartzJobDetails(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QQuartzJobDetails(PathMetadata metadata, PathInits inits) {
        this(QuartzJobDetails.class, metadata, inits);
    }

    public QQuartzJobDetails(Class<? extends QuartzJobDetails> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.id = inits.isInitialized("id") ? new QQuartzJobDetails_Id(forProperty("id")) : null;
    }

}

