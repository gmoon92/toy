package com.gmoon.springjpapagination.users.userloginlog.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUserLoginLog is a Querydsl query type for UserLoginLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserLoginLog extends EntityPathBase<UserLoginLog> {

    private static final long serialVersionUID = 888130247L;

    public static final QUserLoginLog userLoginLog = new QUserLoginLog("userLoginLog");

    public final EnumPath<AccessDevice> accessDevice = createEnum("accessDevice", AccessDevice.class);

    public final DateTimePath<java.time.Instant> attemptAt = createDateTime("attemptAt", java.time.Instant.class);

    public final StringPath attemptIp = createString("attemptIp");

    public final StringPath id = createString("id");

    public final BooleanPath succeed = createBoolean("succeed");

    public final StringPath username = createString("username");

    public QUserLoginLog(String variable) {
        super(UserLoginLog.class, forVariable(variable));
    }

    public QUserLoginLog(Path<? extends UserLoginLog> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserLoginLog(PathMetadata metadata) {
        super(UserLoginLog.class, metadata);
    }

}

