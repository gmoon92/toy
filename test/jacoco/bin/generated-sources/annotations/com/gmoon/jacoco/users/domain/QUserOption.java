package com.gmoon.jacoco.users.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserOption is a Querydsl query type for UserOption
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserOption extends EntityPathBase<UserOption> {

    private static final long serialVersionUID = -155029768L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserOption userOption = new QUserOption("userOption");

    public final BooleanPath allowsReceivingMail = createBoolean("allowsReceivingMail");

    public final QUser user;

    public final StringPath userId = createString("userId");

    public QUserOption(String variable) {
        this(UserOption.class, forVariable(variable), INITS);
    }

    public QUserOption(Path<? extends UserOption> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserOption(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserOption(PathMetadata metadata, PathInits inits) {
        this(UserOption.class, metadata, inits);
    }

    public QUserOption(Class<? extends UserOption> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user"), inits.get("user")) : null;
    }

}

