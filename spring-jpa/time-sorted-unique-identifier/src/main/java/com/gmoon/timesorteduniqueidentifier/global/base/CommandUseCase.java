package com.gmoon.timesorteduniqueidentifier.global.base;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Transactional
@UseCase
public @interface CommandUseCase {

	@AliasFor(annotation = Component.class)
	String value() default "";
}
