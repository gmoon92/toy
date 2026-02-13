package com.gmoon.hibernateperformance.applyform.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QApplyForm is a Querydsl query type for ApplyForm
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QApplyForm extends EntityPathBase<ApplyForm> {

    private static final long serialVersionUID = -866274719L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QApplyForm applyForm = new QApplyForm("applyForm");

    public final StringPath content = createString("content");

    public final QApplyForm_Id id;

    public final com.gmoon.hibernateperformance.member.domain.QMember member;

    public final com.gmoon.hibernateperformance.team.domain.QTeam team;

    public final StringPath title = createString("title");

    public QApplyForm(String variable) {
        this(ApplyForm.class, forVariable(variable), INITS);
    }

    public QApplyForm(Path<? extends ApplyForm> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QApplyForm(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QApplyForm(PathMetadata metadata, PathInits inits) {
        this(ApplyForm.class, metadata, inits);
    }

    public QApplyForm(Class<? extends ApplyForm> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.id = inits.isInitialized("id") ? new QApplyForm_Id(forProperty("id")) : null;
        this.member = inits.isInitialized("member") ? new com.gmoon.hibernateperformance.member.domain.QMember(forProperty("member"), inits.get("member")) : null;
        this.team = inits.isInitialized("team") ? new com.gmoon.hibernateperformance.team.domain.QTeam(forProperty("team")) : null;
    }

}

