package com.gmoon.springquartzcluster.quartz.job_store.id;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QQuartzTriggerId is a Querydsl query type for QuartzTriggerId
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QQuartzTriggerId extends BeanPath<QuartzTriggerId> {

    private static final long serialVersionUID = -1511503112L;

    public static final QQuartzTriggerId quartzTriggerId = new QQuartzTriggerId("quartzTriggerId");

    public final QQuartzId _super = new QQuartzId(this);

    //inherited
    public final StringPath schedulerName = _super.schedulerName;

    public final StringPath triggerGroup = createString("triggerGroup");

    public final StringPath triggerName = createString("triggerName");

    public QQuartzTriggerId(String variable) {
        super(QuartzTriggerId.class, forVariable(variable));
    }

    public QQuartzTriggerId(Path<? extends QuartzTriggerId> path) {
        super(path.getType(), path.getMetadata());
    }

    public QQuartzTriggerId(PathMetadata metadata) {
        super(QuartzTriggerId.class, metadata);
    }

}

