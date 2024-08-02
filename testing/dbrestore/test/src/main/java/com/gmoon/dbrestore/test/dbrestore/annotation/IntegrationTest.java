package com.gmoon.dbrestore.test.dbrestore.annotation;

import org.springframework.boot.test.context.SpringBootTest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@DataRestorer
@SpringBootTest
@Target(value = ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface IntegrationTest {

}
