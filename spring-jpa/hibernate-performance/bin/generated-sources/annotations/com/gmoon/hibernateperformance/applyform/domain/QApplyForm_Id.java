package com.gmoon.hibernateperformance.applyform.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QApplyForm_Id is a Querydsl query type for Id
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QApplyForm_Id extends BeanPath<ApplyForm.Id> {

    private static final long serialVersionUID = 1268374504L;

    public static final QApplyForm_Id id = new QApplyForm_Id("id");

    public final com.gmoon.hibernateperformance.global.base.QEntityId _super = new com.gmoon.hibernateperformance.global.base.QEntityId(this);

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    public final NumberPath<Long> teamId = createNumber("teamId", Long.class);

    public QApplyForm_Id(String variable) {
        super(ApplyForm.Id.class, forVariable(variable));
    }

    public QApplyForm_Id(Path<? extends ApplyForm.Id> path) {
        super(path.getType(), path.getMetadata());
    }

    public QApplyForm_Id(PathMetadata metadata) {
        super(ApplyForm.Id.class, metadata);
    }

}

