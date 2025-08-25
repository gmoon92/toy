package com.gmoon.springpoi.common.excel.vo;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;

import com.gmoon.springpoi.common.excel.annotation.ExcelComponent;
import com.gmoon.springpoi.common.excel.annotation.ExcelProperty;
import com.gmoon.springpoi.common.excel.converter.ExcelConverter;
import com.gmoon.springpoi.common.excel.validator.ExcelBatchValidator;
import com.gmoon.springpoi.common.excel.validator.ExcelValidator;
import com.gmoon.springpoi.common.excel.validator.common.RequiredValueValidator;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class ExcelField {
	private final int cellColIndex;
	private final Field field;
	private final boolean required;
	private final List<ExcelValidator> validators;
	private final List<ExcelBatchValidator> batchValidators;
	private final ExcelConverter<?> converter;

	ExcelField(int cellColIndex, Field field, ApplicationContext ctx) {
		this.cellColIndex = cellColIndex;
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
			 .filter(clazz -> AnnotationUtils.findAnnotation(clazz, ExcelComponent.class) != null)
			 .map(ctx::getBean)
			 .map(typeFilter::cast)
			 .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
	}

	private ExcelConverter<?> getConverter(ExcelProperty annotation, ApplicationContext ctx) {
		Class<? extends ExcelConverter<?>> clazz = annotation.converter();
		if (AnnotationUtils.findAnnotation(clazz, ExcelComponent.class) == null) {
			throw new RuntimeException("@ExcelComponent is required");
		}

		return ctx.getBean(clazz);
	}

	public List<ExcelValidator> getFailedValidators(String cellValue) {
		boolean shouldSkipValidation = !isRequired() && cellValue == null;
		if (shouldSkipValidation) {
			return Collections.emptyList();
		}

		if (isRequired() && StringUtils.isBlank(cellValue)) {
			return Collections.singletonList(RequiredValueValidator.INSTANCE);
		}

		return getValidators()
			 .stream()
			 .filter(validator -> !validator.isValid(cellValue))
			 .peek(validator -> log.debug(
					   "[excel validation failed] validator: {}, {}: {}",
					   validator,
					   getFieldName(),
					   cellValue
				  )
			 )
			 .toList();
	}

	public String getFieldName() {
		return field.getName();
	}
}
