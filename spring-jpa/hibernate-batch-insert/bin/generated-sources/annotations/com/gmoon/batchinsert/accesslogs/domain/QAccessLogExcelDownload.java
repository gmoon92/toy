package com.gmoon.batchinsert.accesslogs.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAccessLogExcelDownload is a Querydsl query type for AccessLogExcelDownload
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAccessLogExcelDownload extends EntityPathBase<AccessLogExcelDownload> {

    private static final long serialVersionUID = -334499364L;

    public static final QAccessLogExcelDownload accessLogExcelDownload = new QAccessLogExcelDownload("accessLogExcelDownload");

    public final StringPath attemptDt = createString("attemptDt");

    public final StringPath id = createString("id");

    public final StringPath ip = createString("ip");

    public final StringPath os = createString("os");

    public final StringPath username = createString("username");

    public QAccessLogExcelDownload(String variable) {
        super(AccessLogExcelDownload.class, forVariable(variable));
    }

    public QAccessLogExcelDownload(Path<? extends AccessLogExcelDownload> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAccessLogExcelDownload(PathMetadata metadata) {
        super(AccessLogExcelDownload.class, metadata);
    }

}

