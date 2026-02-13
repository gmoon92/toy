package com.gmoon.batchinsert.accesslogs.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAccessLog is a Querydsl query type for AccessLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAccessLog extends EntityPathBase<AccessLog> {

    private static final long serialVersionUID = 437828419L;

    public static final QAccessLog accessLog = new QAccessLog("accessLog");

    public final DateTimePath<java.time.Instant> attemptAt = createDateTime("attemptAt", java.time.Instant.class);

    public final StringPath id = createString("id");

    public final StringPath ip = createString("ip");

    public final EnumPath<com.gmoon.batchinsert.accesslogs.domain.vo.OperatingSystem> os = createEnum("os", com.gmoon.batchinsert.accesslogs.domain.vo.OperatingSystem.class);

    public final StringPath username = createString("username");

    public QAccessLog(String variable) {
        super(AccessLog.class, forVariable(variable));
    }

    public QAccessLog(Path<? extends AccessLog> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAccessLog(PathMetadata metadata) {
        super(AccessLog.class, metadata);
    }

}

