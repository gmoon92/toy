package com.gmoon.hibernateperformance.team.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTeam is a Querydsl query type for Team
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTeam extends EntityPathBase<Team> {

    private static final long serialVersionUID = 284977203L;

    public static final QTeam team = new QTeam("team");

    public final ListPath<com.gmoon.hibernateperformance.applyform.domain.ApplyForm, com.gmoon.hibernateperformance.applyform.domain.QApplyForm> applyForms = this.<com.gmoon.hibernateperformance.applyform.domain.ApplyForm, com.gmoon.hibernateperformance.applyform.domain.QApplyForm>createList("applyForms", com.gmoon.hibernateperformance.applyform.domain.ApplyForm.class, com.gmoon.hibernateperformance.applyform.domain.QApplyForm.class, PathInits.DIRECT2);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final SetPath<TeamMember, QTeamMember> teamMembers = this.<TeamMember, QTeamMember>createSet("teamMembers", TeamMember.class, QTeamMember.class, PathInits.DIRECT2);

    public QTeam(String variable) {
        super(Team.class, forVariable(variable));
    }

    public QTeam(Path<? extends Team> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTeam(PathMetadata metadata) {
        super(Team.class, metadata);
    }

}

