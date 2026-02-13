package com.gmoon.hibernateperformance.member.model;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.gmoon.hibernateperformance.member.model.QMembers_Data is a Querydsl Projection type for Data
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QMembers_Data extends ConstructorExpression<Members.Data> {

    private static final long serialVersionUID = -617704997L;

    public QMembers_Data(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<String> name) {
        super(Members.Data.class, new Class<?>[]{long.class, String.class}, id, name);
    }

    public QMembers_Data(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<String> name, com.querydsl.core.types.Expression<Boolean> enabled) {
        super(Members.Data.class, new Class<?>[]{long.class, String.class, boolean.class}, id, name, enabled);
    }

}

