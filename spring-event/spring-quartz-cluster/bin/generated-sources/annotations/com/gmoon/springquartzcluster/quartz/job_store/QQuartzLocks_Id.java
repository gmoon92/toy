package com.gmoon.springquartzcluster.quartz.job_store;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QQuartzLocks_Id is a Querydsl query type for Id
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QQuartzLocks_Id extends BeanPath<QuartzLocks.Id> {

    private static final long serialVersionUID = 1465951983L;

    public static final QQuartzLocks_Id id = new QQuartzLocks_Id("id");

    public final com.gmoon.springquartzcluster.quartz.job_store.id.QQuartzId _super = new com.gmoon.springquartzcluster.quartz.job_store.id.QQuartzId(this);

    public final StringPath lockName = createString("lockName");

    //inherited
    public final StringPath schedulerName = _super.schedulerName;

    public QQuartzLocks_Id(String variable) {
        super(QuartzLocks.Id.class, forVariable(variable));
    }

    public QQuartzLocks_Id(Path<? extends QuartzLocks.Id> path) {
        super(path.getType(), path.getMetadata());
    }

    public QQuartzLocks_Id(PathMetadata metadata) {
        super(QuartzLocks.Id.class, metadata);
    }

}

