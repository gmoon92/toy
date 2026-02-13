package com.gmoon.hibernateperformance.member.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMember is a Querydsl query type for Member
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMember extends EntityPathBase<Member> {

    private static final long serialVersionUID = -1908590291L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMember member = new QMember("member1");

    public final ListPath<com.gmoon.hibernateperformance.applyform.domain.ApplyForm, com.gmoon.hibernateperformance.applyform.domain.QApplyForm> applyForm = this.<com.gmoon.hibernateperformance.applyform.domain.ApplyForm, com.gmoon.hibernateperformance.applyform.domain.QApplyForm>createList("applyForm", com.gmoon.hibernateperformance.applyform.domain.ApplyForm.class, com.gmoon.hibernateperformance.applyform.domain.QApplyForm.class, PathInits.DIRECT2);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QMemberOption memberOption;

    public final StringPath name = createString("name");

    public final SetPath<com.gmoon.hibernateperformance.team.domain.TeamMember, com.gmoon.hibernateperformance.team.domain.QTeamMember> teamMembers = this.<com.gmoon.hibernateperformance.team.domain.TeamMember, com.gmoon.hibernateperformance.team.domain.QTeamMember>createSet("teamMembers", com.gmoon.hibernateperformance.team.domain.TeamMember.class, com.gmoon.hibernateperformance.team.domain.QTeamMember.class, PathInits.DIRECT2);

    public QMember(String variable) {
        this(Member.class, forVariable(variable), INITS);
    }

    public QMember(Path<? extends Member> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMember(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMember(PathMetadata metadata, PathInits inits) {
        this(Member.class, metadata, inits);
    }

    public QMember(Class<? extends Member> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.memberOption = inits.isInitialized("memberOption") ? new QMemberOption(forProperty("memberOption"), inits.get("memberOption")) : null;
    }

}

