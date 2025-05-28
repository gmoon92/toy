package com.gmoon.batchinsert.global.meta.resolver;

import javax.lang.model.element.Element;

import com.gmoon.batchinsert.global.meta.utils.AnnotationProcessorUtils;
import com.gmoon.javacore.util.StringUtils;

import jakarta.persistence.Column;

public class AnnotationColumnNameResolver implements ColumnNameResolver {

	private final String columnName;

	public AnnotationColumnNameResolver(Element field) {
		this.columnName = AnnotationProcessorUtils.getAnnotationAttributeValue(
			 field,
			 Column.class,
			 "name",
			 String.class
		);
	}

	@Override
	public boolean supports() {
		return StringUtils.isNotBlank(columnName);
	}

	@Override
	public String resolve() {
		return columnName;
	}
}
