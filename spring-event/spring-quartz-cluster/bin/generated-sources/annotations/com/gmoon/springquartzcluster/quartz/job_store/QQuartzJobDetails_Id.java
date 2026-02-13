package com.gmoon.springquartzcluster.quartz.job_store;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QQuartzJobDetails_Id is a Querydsl query type for Id
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QQuartzJobDetails_Id extends BeanPath<QuartzJobDetails.Id> {

    private static final long serialVersionUID = -940884458L;

    public static final QQuartzJobDetails_Id id = new QQuartzJobDetails_Id("id");

    public final com.gmoon.springquartzcluster.quartz.job_store.id.QQuartzId _super = new com.gmoon.springquartzcluster.quartz.job_store.id.QQuartzId(this);

    public final StringPath jobGroup = createString("jobGroup");

    public final StringPath jobName = createString("jobName");

    //inherited
    public final StringPath schedulerName = _super.schedulerName;

    public QQuartzJobDetails_Id(String variable) {
        super(QuartzJobDetails.Id.class, forVariable(variable));
    }

    public QQuartzJobDetails_Id(Path<? extends QuartzJobDetails.Id> path) {
        super(path.getType(), path.getMetadata());
    }

    public QQuartzJobDetails_Id(PathMetadata metadata) {
        super(QuartzJobDetails.Id.class, metadata);
    }

}

