package com.gmoon.springquartzcluster.quartz.job_store;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QQuartzCalendars is a Querydsl query type for QuartzCalendars
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuartzCalendars extends EntityPathBase<QuartzCalendars> {

    private static final long serialVersionUID = -2119610361L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QQuartzCalendars quartzCalendars = new QQuartzCalendars("quartzCalendars");

    public final ArrayPath<byte[], Byte> calendar = createArray("calendar", byte[].class);

    public final QQuartzCalendars_Id id;

    public QQuartzCalendars(String variable) {
        this(QuartzCalendars.class, forVariable(variable), INITS);
    }

    public QQuartzCalendars(Path<? extends QuartzCalendars> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QQuartzCalendars(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QQuartzCalendars(PathMetadata metadata, PathInits inits) {
        this(QuartzCalendars.class, metadata, inits);
    }

    public QQuartzCalendars(Class<? extends QuartzCalendars> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.id = inits.isInitialized("id") ? new QQuartzCalendars_Id(forProperty("id")) : null;
    }

}

