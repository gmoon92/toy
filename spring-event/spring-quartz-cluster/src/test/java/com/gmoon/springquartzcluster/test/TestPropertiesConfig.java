package com.gmoon.springquartzcluster.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.gmoon.springquartzcluster.config.PropertiesConfig;

@Import(PropertiesConfig.class)
@TestPropertySource(value = "classpath:quartz.yml", factory = YamlPropertySourceFactory.class)
@Inherited
@SpringJUnitConfig
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TestPropertiesConfig {
	@AliasFor(annotation = SpringJUnitConfig.class, attribute = "classes")
	Class<?>[] value() default {};

	@AliasFor(annotation = SpringJUnitConfig.class)
	Class<?>[] classes() default {};

	@AliasFor(annotation = ContextConfiguration.class)
	boolean inheritLocations() default true;

	@AliasFor(annotation = ContextConfiguration.class)
	boolean inheritInitializers() default true;
}
