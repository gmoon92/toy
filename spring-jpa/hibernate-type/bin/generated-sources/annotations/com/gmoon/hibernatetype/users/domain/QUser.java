package com.gmoon.hibernatetype.users.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.Expression;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = 924574810L;

    public static ConstructorExpression<User> create(Expression<String> email, Expression<String> encEmail) {
        return Projections.constructor(User.class, new Class<?>[]{String.class, String.class}, email, encEmail);
    }

    public static final QUser user = new QUser("user");

    public final StringPath email = createString("email");

    public final StringPath encEmail = createString("encEmail");

    public final StringPath id = createString("id");

    public final EnumPath<UserStatus> status = createEnum("status", UserStatus.class);

    public QUser(String variable) {
        super(User.class, forVariable(variable));
    }

    public QUser(Path<? extends User> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUser(PathMetadata metadata) {
        super(User.class, metadata);
    }

}

