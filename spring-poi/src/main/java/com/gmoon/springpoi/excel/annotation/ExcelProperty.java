package com.gmoon.springpoi.excel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.gmoon.springpoi.excel.converter.ExcelConverter;
import com.gmoon.springpoi.excel.converter.common.StringXssSafeConverter;
import com.gmoon.springpoi.excel.validator.ExcelValidator;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExcelProperty {

	String title() default "";

	String comment() default "";

	boolean required() default false;

	Class<? extends ExcelValidator>[] validator() default {};

	Class<? extends ExcelConverter<?>> converter() default StringXssSafeConverter.class;
}
