package com.gmoon.springquartzcluster.quartz.job_store;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QQuartzSimpropTriggers is a Querydsl query type for QuartzSimpropTriggers
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuartzSimpropTriggers extends EntityPathBase<QuartzSimpropTriggers> {

    private static final long serialVersionUID = 234837543L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QQuartzSimpropTriggers quartzSimpropTriggers = new QQuartzSimpropTriggers("quartzSimpropTriggers");

    public final StringPath boolProp1 = createString("boolProp1");

    public final StringPath boolProp2 = createString("boolProp2");

    public final NumberPath<Long> decProp1 = createNumber("decProp1", Long.class);

    public final NumberPath<Long> decProp2 = createNumber("decProp2", Long.class);

    public final com.gmoon.springquartzcluster.quartz.job_store.id.QQuartzTriggerId id;

    public final NumberPath<Integer> intProp1 = createNumber("intProp1", Integer.class);

    public final NumberPath<Integer> intProp2 = createNumber("intProp2", Integer.class);

    public final NumberPath<Long> longProp1 = createNumber("longProp1", Long.class);

    public final NumberPath<Long> longProp2 = createNumber("longProp2", Long.class);

    public final StringPath strProp1 = createString("strProp1");

    public final StringPath strProp2 = createString("strProp2");

    public final StringPath strProp3 = createString("strProp3");

    public final QQuartzTriggers triggers;

    public QQuartzSimpropTriggers(String variable) {
        this(QuartzSimpropTriggers.class, forVariable(variable), INITS);
    }

    public QQuartzSimpropTriggers(Path<? extends QuartzSimpropTriggers> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QQuartzSimpropTriggers(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QQuartzSimpropTriggers(PathMetadata metadata, PathInits inits) {
        this(QuartzSimpropTriggers.class, metadata, inits);
    }

    public QQuartzSimpropTriggers(Class<? extends QuartzSimpropTriggers> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.id = inits.isInitialized("id") ? new com.gmoon.springquartzcluster.quartz.job_store.id.QQuartzTriggerId(forProperty("id")) : null;
        this.triggers = inits.isInitialized("triggers") ? new QQuartzTriggers(forProperty("triggers"), inits.get("triggers")) : null;
    }

}

