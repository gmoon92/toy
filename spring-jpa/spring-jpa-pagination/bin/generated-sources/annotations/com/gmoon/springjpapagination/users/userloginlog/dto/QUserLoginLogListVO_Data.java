package com.gmoon.springjpapagination.users.userloginlog.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.gmoon.springjpapagination.users.userloginlog.dto.QUserLoginLogListVO_Data is a Querydsl Projection type for Data
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QUserLoginLogListVO_Data extends ConstructorExpression<UserLoginLogListVO.Data> {

    private static final long serialVersionUID = -113686749L;

    public QUserLoginLogListVO_Data(com.querydsl.core.types.Expression<String> id, com.querydsl.core.types.Expression<String> username, com.querydsl.core.types.Expression<com.gmoon.springjpapagination.users.userloginlog.domain.AccessDevice> accessDevice, com.querydsl.core.types.Expression<java.time.Instant> attemptAt, com.querydsl.core.types.Expression<String> attemptIp, com.querydsl.core.types.Expression<Boolean> succeed) {
        super(UserLoginLogListVO.Data.class, new Class<?>[]{String.class, String.class, com.gmoon.springjpapagination.users.userloginlog.domain.AccessDevice.class, java.time.Instant.class, String.class, boolean.class}, id, username, accessDevice, attemptAt, attemptIp, succeed);
    }

}

