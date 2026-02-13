package com.gmoon.hibernateenvers.member.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMemberCustom is a Querydsl query type for MemberCustom
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMemberCustom extends EntityPathBase<MemberCustom> {

    private static final long serialVersionUID = 1206957207L;

    public static final QMemberCustom memberCustom = new QMemberCustom("memberCustom");

    public final QMember _super = new QMember(this);

    public final StringPath accessAccountFailCount = createString("accessAccountFailCount");

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final StringPath createdBy = _super.createdBy;

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final DateTimePath<java.time.Instant> modifiedAt = _super.modifiedAt;

    //inherited
    public final StringPath modifiedBy = _super.modifiedBy;

    //inherited
    public final StringPath password = _super.password;

    //inherited
    public final StringPath username = _super.username;

    public QMemberCustom(String variable) {
        super(MemberCustom.class, forVariable(variable));
    }

    public QMemberCustom(Path<? extends MemberCustom> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMemberCustom(PathMetadata metadata) {
        super(MemberCustom.class, metadata);
    }

}

