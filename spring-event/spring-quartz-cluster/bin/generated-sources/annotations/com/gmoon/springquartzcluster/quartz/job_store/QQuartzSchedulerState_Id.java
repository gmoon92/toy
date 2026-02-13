package com.gmoon.springquartzcluster.quartz.job_store;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QQuartzSchedulerState_Id is a Querydsl query type for Id
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QQuartzSchedulerState_Id extends BeanPath<QuartzSchedulerState.Id> {

    private static final long serialVersionUID = -1921366363L;

    public static final QQuartzSchedulerState_Id id = new QQuartzSchedulerState_Id("id");

    public final com.gmoon.springquartzcluster.quartz.job_store.id.QQuartzId _super = new com.gmoon.springquartzcluster.quartz.job_store.id.QQuartzId(this);

    public final StringPath instanceName = createString("instanceName");

    //inherited
    public final StringPath schedulerName = _super.schedulerName;

    public QQuartzSchedulerState_Id(String variable) {
        super(QuartzSchedulerState.Id.class, forVariable(variable));
    }

    public QQuartzSchedulerState_Id(Path<? extends QuartzSchedulerState.Id> path) {
        super(path.getType(), path.getMetadata());
    }

    public QQuartzSchedulerState_Id(PathMetadata metadata) {
        super(QuartzSchedulerState.Id.class, metadata);
    }

}

