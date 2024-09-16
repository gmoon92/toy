package com.gmoon.timesorteduniqueidentifier.global.test.annotation;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@JpaFlushAndClear
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public @interface PersistenceAdapterTest {

	@AliasFor(annotation = Import.class, attribute = "value")
	Class<?>[] value() default {};

	@AliasFor(annotation = JpaFlushAndClear.class, attribute = "enabled")
	boolean flushAndClear() default true;
}
