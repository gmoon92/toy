package com.gmoon.payment.test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Import(TestConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UnitTestCase {

	@AliasFor(annotation = ContextConfiguration.class, attribute = "classes")
	Class<?>[] value() default {};
}
