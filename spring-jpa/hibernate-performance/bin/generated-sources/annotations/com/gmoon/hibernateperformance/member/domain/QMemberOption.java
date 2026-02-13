package com.gmoon.hibernateperformance.member.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMemberOption is a Querydsl query type for MemberOption
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMemberOption extends EntityPathBase<MemberOption> {

    private static final long serialVersionUID = 1230395394L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMemberOption memberOption = new QMemberOption("memberOption");

    public final QEnabled enabled;

    public final QMember member;

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    public final BooleanPath retired = createBoolean("retired");

    public QMemberOption(String variable) {
        this(MemberOption.class, forVariable(variable), INITS);
    }

    public QMemberOption(Path<? extends MemberOption> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMemberOption(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMemberOption(PathMetadata metadata, PathInits inits) {
        this(MemberOption.class, metadata, inits);
    }

    public QMemberOption(Class<? extends MemberOption> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.enabled = inits.isInitialized("enabled") ? new QEnabled(forProperty("enabled")) : null;
        this.member = inits.isInitialized("member") ? new QMember(forProperty("member"), inits.get("member")) : null;
    }

}

