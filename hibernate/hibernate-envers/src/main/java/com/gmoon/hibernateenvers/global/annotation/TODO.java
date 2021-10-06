package com.gmoon.hibernateenvers.global.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Target( { ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.METHOD })
@Retention(RetentionPolicy.SOURCE)
public @interface TODO {

  @AliasFor(value = "remark")
  String value() default "";

  @AliasFor(value = "value")
  String remark() default "";

  String reference() default "";
}
