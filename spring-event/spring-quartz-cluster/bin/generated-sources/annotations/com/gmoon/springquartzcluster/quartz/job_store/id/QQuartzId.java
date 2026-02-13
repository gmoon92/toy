package com.gmoon.springquartzcluster.quartz.job_store.id;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QQuartzId is a Querydsl query type for QuartzId
 */
@Generated("com.querydsl.codegen.DefaultSupertypeSerializer")
public class QQuartzId extends EntityPathBase<QuartzId> {

    private static final long serialVersionUID = 445488822L;

    public static final QQuartzId quartzId = new QQuartzId("quartzId");

    public final StringPath schedulerName = createString("schedulerName");

    public QQuartzId(String variable) {
        super(QuartzId.class, forVariable(variable));
    }

    public QQuartzId(Path<? extends QuartzId> path) {
        super(path.getType(), path.getMetadata());
    }

    public QQuartzId(PathMetadata metadata) {
        super(QuartzId.class, metadata);
    }

}

