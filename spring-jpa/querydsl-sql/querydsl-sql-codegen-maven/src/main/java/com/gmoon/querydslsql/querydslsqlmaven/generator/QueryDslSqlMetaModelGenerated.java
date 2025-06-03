package com.gmoon.querydslsql.querydslsqlmaven.generator;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface QueryDslSqlMetaModelGenerated {
	String value() default "";
}
