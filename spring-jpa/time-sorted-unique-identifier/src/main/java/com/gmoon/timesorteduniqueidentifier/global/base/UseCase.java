package com.gmoon.timesorteduniqueidentifier.global.base;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@Inherited
@interface UseCase {

	@AliasFor(annotation = Component.class)
	String value() default "";
}
