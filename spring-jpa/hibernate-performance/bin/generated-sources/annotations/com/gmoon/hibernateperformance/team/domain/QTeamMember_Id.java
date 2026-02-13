package com.gmoon.hibernateperformance.team.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QTeamMember_Id is a Querydsl query type for Id
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QTeamMember_Id extends BeanPath<TeamMember.Id> {

    private static final long serialVersionUID = -1836306916L;

    public static final QTeamMember_Id id = new QTeamMember_Id("id");

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    public final NumberPath<Long> teamId = createNumber("teamId", Long.class);

    public QTeamMember_Id(String variable) {
        super(TeamMember.Id.class, forVariable(variable));
    }

    public QTeamMember_Id(Path<? extends TeamMember.Id> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTeamMember_Id(PathMetadata metadata) {
        super(TeamMember.Id.class, metadata);
    }

}

