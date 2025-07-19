package com.gmoon.commons.commonsapachepoi.excel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

@Scope
@Component
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExcelComponent {

	@AliasFor(annotation = Component.class, value = "value")
	String value() default "";

	@AliasFor(annotation = Scope.class, value = "scopeName")
	String scopeName() default BeanDefinition.SCOPE_SINGLETON;

	@AliasFor(annotation = Scope.class)
	ScopedProxyMode proxyMode() default ScopedProxyMode.DEFAULT;
}
