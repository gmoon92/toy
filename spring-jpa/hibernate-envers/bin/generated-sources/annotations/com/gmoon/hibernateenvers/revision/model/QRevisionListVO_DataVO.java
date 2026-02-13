package com.gmoon.hibernateenvers.revision.model;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.gmoon.hibernateenvers.revision.model.QRevisionListVO_DataVO is a Querydsl Projection type for DataVO
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QRevisionListVO_DataVO extends ConstructorExpression<RevisionListVO.DataVO> {

    private static final long serialVersionUID = 491225053L;

    public QRevisionListVO_DataVO(com.querydsl.core.types.Expression<Long> revisionId, com.querydsl.core.types.Expression<java.time.Instant> revisionAt, com.querydsl.core.types.Expression<? extends byte[]> entityId, com.querydsl.core.types.Expression<com.gmoon.hibernateenvers.revision.domain.vo.RevisionTarget> revisionTarget, com.querydsl.core.types.Expression<String> updatedBy, com.querydsl.core.types.Expression<String> updatedByUsername, com.querydsl.core.types.Expression<String> targetMemberName) {
        super(RevisionListVO.DataVO.class, new Class<?>[]{long.class, java.time.Instant.class, byte[].class, com.gmoon.hibernateenvers.revision.domain.vo.RevisionTarget.class, String.class, String.class, String.class}, revisionId, revisionAt, entityId, revisionTarget, updatedBy, updatedByUsername, targetMemberName);
    }

}

