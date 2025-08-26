package com.gmoon.springaccesslog.http.annotation;

import com.gmoon.springaccesslog.http.constant.ApiRequestFieldName;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiRequestField {

	ApiRequestFieldName value();
}
