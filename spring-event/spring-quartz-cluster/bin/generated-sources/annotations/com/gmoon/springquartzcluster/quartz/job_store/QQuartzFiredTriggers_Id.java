package com.gmoon.springquartzcluster.quartz.job_store;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QQuartzFiredTriggers_Id is a Querydsl query type for Id
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QQuartzFiredTriggers_Id extends BeanPath<QuartzFiredTriggers.Id> {

    private static final long serialVersionUID = 1984835886L;

    public static final QQuartzFiredTriggers_Id id = new QQuartzFiredTriggers_Id("id");

    public final com.gmoon.springquartzcluster.quartz.job_store.id.QQuartzId _super = new com.gmoon.springquartzcluster.quartz.job_store.id.QQuartzId(this);

    public final StringPath entryId = createString("entryId");

    //inherited
    public final StringPath schedulerName = _super.schedulerName;

    public QQuartzFiredTriggers_Id(String variable) {
        super(QuartzFiredTriggers.Id.class, forVariable(variable));
    }

    public QQuartzFiredTriggers_Id(Path<? extends QuartzFiredTriggers.Id> path) {
        super(path.getType(), path.getMetadata());
    }

    public QQuartzFiredTriggers_Id(PathMetadata metadata) {
        super(QuartzFiredTriggers.Id.class, metadata);
    }

}

