package com.gmoon.springquartzcluster.quartz.job_store;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QQuartzCalendars_Id is a Querydsl query type for Id
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QQuartzCalendars_Id extends BeanPath<QuartzCalendars.Id> {

    private static final long serialVersionUID = -703032190L;

    public static final QQuartzCalendars_Id id = new QQuartzCalendars_Id("id");

    public final com.gmoon.springquartzcluster.quartz.job_store.id.QQuartzId _super = new com.gmoon.springquartzcluster.quartz.job_store.id.QQuartzId(this);

    public final StringPath calendarName = createString("calendarName");

    //inherited
    public final StringPath schedulerName = _super.schedulerName;

    public QQuartzCalendars_Id(String variable) {
        super(QuartzCalendars.Id.class, forVariable(variable));
    }

    public QQuartzCalendars_Id(Path<? extends QuartzCalendars.Id> path) {
        super(path.getType(), path.getMetadata());
    }

    public QQuartzCalendars_Id(PathMetadata metadata) {
        super(QuartzCalendars.Id.class, metadata);
    }

}

