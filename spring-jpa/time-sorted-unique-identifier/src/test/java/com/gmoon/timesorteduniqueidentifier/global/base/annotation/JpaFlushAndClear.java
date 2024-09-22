package com.gmoon.timesorteduniqueidentifier.global.base.annotation;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

@ExtendWith(JpaFlushClearAfterTestExtension.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@interface JpaFlushAndClear {

	boolean enabled() default false;
}
