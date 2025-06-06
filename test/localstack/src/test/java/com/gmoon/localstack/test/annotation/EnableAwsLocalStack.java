package com.gmoon.localstack.test.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.testcontainers.containers.localstack.LocalStackContainer;

import com.gmoon.localstack.test.config.AwsLocalStackConfig;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(AwsLocalStackConfig.class)
public @interface EnableAwsLocalStack {

	@AliasFor("services")
	LocalStackContainer.Service[] value() default {};

	@AliasFor("value")
	LocalStackContainer.Service[] services() default {};

	String imageFullName() default "localstack/localstack:0.14.3";
}
