package com.gmoon.commons.commonsapachepoi.excel.vo;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import com.gmoon.commons.commonsapachepoi.common.utils.ReflectionUtil;
import com.gmoon.commons.commonsapachepoi.excel.annotation.ExcelModel;
import com.gmoon.commons.commonsapachepoi.excel.annotation.ExcelProperty;
import com.gmoon.commons.commonsapachepoi.excel.validator.ExcelBatchValidator;

import lombok.Getter;

@Getter
public class ExcelFields {
	private final ExcelModel excelModelAnnotation;
	private final Map<Integer, ExcelField> value;

	private ExcelFields(Class<?> excelModelClass, ApplicationContext ctx, String... excludeFieldNames) {
		excelModelAnnotation = getExcelModelAnnotation(excelModelClass);
		value = ReflectionUtil.getFieldMap(
			 excelModelClass,
			 field -> {
				 ExcelProperty annotation = field.getAnnotation(ExcelProperty.class);
				 return annotation != null && !isExcludeField(field, excludeFieldNames);
			 },
			 field -> new ExcelField(field, ctx)
		);

		if (value.isEmpty()) {
			throw new UnsupportedOperationException(
				 String.format("@ExcelProperty annotation not found in class %s", excelModelClass.getName())
			);
		}
	}

	public static ExcelFields of(Class<?> clazz, ApplicationContext ctx, String... excludeFieldNames) {
		return new ExcelFields(clazz, ctx, excludeFieldNames);
	}

	public List<ExcelBatchValidator> getAllBatchValidators() {
		return value.values()
			 .stream()
			 .map(ExcelField::getBatchValidators)
			 .flatMap(Collection::stream)
			 .toList();
	}

	private boolean isExcludeField(Field field, String... excludeFieldNames) {
		return excludeFieldNames != null && excludeFieldNames.length > 0
			 && StringUtils.equalsAny(field.getName(), excludeFieldNames);
	}

	private ExcelModel getExcelModelAnnotation(Class<?> excelModelClass) {
		return Optional.ofNullable(ReflectionUtil.findAnnotation(excelModelClass, ExcelModel.class))
			 .orElseThrow(() -> new UnsupportedOperationException(
				  String.format("@ExcelModel annotation not found in class %s", excelModelClass.getName())
			 ));
	}

	public int size() {
		return value.size();
	}

	public Set<Map.Entry<Integer, ExcelField>> entrySet() {
		return value.entrySet();
	}

}
