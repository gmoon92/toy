package com.gmoon.junit5.jupiter.argumentssource.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.params.provider.ArgumentsSource;

import com.gmoon.junit5.jupiter.argumentssource.provider.NegativeNumbersProvider;

@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ArgumentsSource(NegativeNumbersProvider.class)
public @interface NegativeNumbers {

	int start() default -10;

	int end() default -1;
}
