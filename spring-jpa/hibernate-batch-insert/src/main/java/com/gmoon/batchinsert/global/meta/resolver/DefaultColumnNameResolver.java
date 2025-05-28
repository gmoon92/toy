package com.gmoon.batchinsert.global.meta.resolver;

import javax.lang.model.element.Element;

import com.gmoon.javacore.util.StringUtils;

public class DefaultColumnNameResolver implements ColumnNameResolver {

	private final String fieldName;

	public DefaultColumnNameResolver(Element field) {
		this.fieldName = field.getSimpleName().toString();
	}

	@Override
	public boolean supports() {
		return true;
	}

	// todo hibernate-naming-strategy 네이밍 전략에 맞게 변환.
	@Override
	public String resolve() {
		return StringUtils.toSnakeCase(fieldName);
	}
}
