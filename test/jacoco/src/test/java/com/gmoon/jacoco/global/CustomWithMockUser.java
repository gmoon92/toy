package com.gmoon.jacoco.global;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.security.test.context.support.WithSecurityContext;

import com.gmoon.jacoco.users.domain.Role;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@WithSecurityContext(factory = CustomWithMockUserSecurityContextFactory.class)
public @interface CustomWithMockUser {

  @AliasFor(attribute = "userId")
  String value() default "";

  String userId() default "";

  Role role() default Role.USER;
}
