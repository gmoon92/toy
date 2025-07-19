package com.gmoon.commons.commonsapachepoi.excel.vo;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;

import com.gmoon.commons.commonsapachepoi.common.utils.ReflectionUtil;
import com.gmoon.commons.commonsapachepoi.excel.annotation.ExcelComponent;
import com.gmoon.commons.commonsapachepoi.excel.annotation.ExcelProperty;
import com.gmoon.commons.commonsapachepoi.excel.converter.ExcelConverter;
import com.gmoon.commons.commonsapachepoi.excel.validator.ExcelBatchValidator;
import com.gmoon.commons.commonsapachepoi.excel.validator.ExcelValidator;

import lombok.Getter;

@Getter
public class ExcelField {
	private final Field field;
	private final boolean required;
	private final List<ExcelValidator> validators;
	private final List<ExcelBatchValidator> batchValidators;
	private final ExcelConverter<?> converter;

	ExcelField(Field field, ApplicationContext ctx) {
		this.field = field;

		ExcelProperty annotation = field.getAnnotation(ExcelProperty.class);
		this.required = annotation.required();
		this.validators = getValidators(annotation, ctx, ExcelValidator.class);
		this.batchValidators = getValidators(annotation, ctx, ExcelBatchValidator.class);
		this.converter = getConverter(annotation, ctx);
	}

	private <T extends ExcelValidator> List<T> getValidators(
		 ExcelProperty annotation,
		 ApplicationContext ctx,
		 Class<T> typeFilter
	) {
		return Arrays.stream(annotation.validators())
			 .filter(typeFilter::isAssignableFrom)
			 .map(clazz -> AnnotationUtils.findAnnotation(clazz, ExcelComponent.class) == null
				  ? ReflectionUtil.newInstance(clazz)
				  : ctx.getBean(clazz))
			 .map(typeFilter::cast)
			 .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
	}

	private ExcelConverter<?> getConverter(ExcelProperty annotation, ApplicationContext ctx) {
		Class<? extends ExcelConverter<?>> clazz = annotation.converter();
		return AnnotationUtils.findAnnotation(clazz, ExcelComponent.class) == null
			 ? ReflectionUtil.newInstance(clazz)
			 : ctx.getBean(clazz);
	}

	public String getFieldName() {
		return field.getName();
	}
}
