package com.gmoon.springpoi.excel.vo;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;

import com.gmoon.springpoi.common.utils.ReflectionUtil;
import com.gmoon.springpoi.excel.annotation.ExcelComponent;
import com.gmoon.springpoi.excel.annotation.ExcelProperty;
import com.gmoon.springpoi.excel.converter.ExcelConverter;
import com.gmoon.springpoi.excel.validator.ExcelBatchValidator;
import com.gmoon.springpoi.excel.validator.ExcelValidator;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
		return Arrays.stream(annotation.validator())
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

	public boolean isValidCellValue(String cellValue) {
		boolean shouldSkipValidation = !isRequired() && cellValue == null;
		if (shouldSkipValidation) {
			return true;
		}

		return getValidators()
			 .stream()
			 .filter(validator -> !validator.isValid(cellValue))
			 .peek(validator -> log.debug("[excel validation failed] validator={}, {}={}",
				  validator,
				  getFieldName(),
				  cellValue
			 ))
			 .findFirst()
			 .isEmpty();
	}

	public String getFieldName() {
		return field.getName();
	}
}
