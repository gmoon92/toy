package com.gmoon.springsecurityjwt.team;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QTeamUser_Id is a Querydsl query type for Id
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QTeamUser_Id extends BeanPath<TeamUser.Id> {

    private static final long serialVersionUID = 1614458519L;

    public static final QTeamUser_Id id = new QTeamUser_Id("id");

    public final NumberPath<Long> teamId = createNumber("teamId", Long.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QTeamUser_Id(String variable) {
        super(TeamUser.Id.class, forVariable(variable));
    }

    public QTeamUser_Id(Path<? extends TeamUser.Id> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTeamUser_Id(PathMetadata metadata) {
        super(TeamUser.Id.class, metadata);
    }

}

