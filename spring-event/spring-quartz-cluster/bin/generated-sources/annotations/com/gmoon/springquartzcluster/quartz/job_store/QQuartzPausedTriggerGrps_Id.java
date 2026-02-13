package com.gmoon.springquartzcluster.quartz.job_store;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QQuartzPausedTriggerGrps_Id is a Querydsl query type for Id
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QQuartzPausedTriggerGrps_Id extends BeanPath<QuartzPausedTriggerGrps.Id> {

    private static final long serialVersionUID = -1910042465L;

    public static final QQuartzPausedTriggerGrps_Id id = new QQuartzPausedTriggerGrps_Id("id");

    public final com.gmoon.springquartzcluster.quartz.job_store.id.QQuartzId _super = new com.gmoon.springquartzcluster.quartz.job_store.id.QQuartzId(this);

    //inherited
    public final StringPath schedulerName = _super.schedulerName;

    public final StringPath triggerGroup = createString("triggerGroup");

    public QQuartzPausedTriggerGrps_Id(String variable) {
        super(QuartzPausedTriggerGrps.Id.class, forVariable(variable));
    }

    public QQuartzPausedTriggerGrps_Id(Path<? extends QuartzPausedTriggerGrps.Id> path) {
        super(path.getType(), path.getMetadata());
    }

    public QQuartzPausedTriggerGrps_Id(PathMetadata metadata) {
        super(QuartzPausedTriggerGrps.Id.class, metadata);
    }

}

