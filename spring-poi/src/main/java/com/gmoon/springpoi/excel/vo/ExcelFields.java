package com.gmoon.springpoi.excel.vo;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import com.gmoon.springpoi.common.utils.ReflectionUtil;
import com.gmoon.springpoi.excel.annotation.ExcelModel;
import com.gmoon.springpoi.excel.annotation.ExcelProperty;
import com.gmoon.springpoi.excel.validator.ExcelBatchValidator;

public class ExcelFields {
	private final ExcelModel excelModel;
	private final Map<Integer, ExcelField> value;

	private ExcelFields(Class<?> excelModelClass, ApplicationContext ctx, String... excludeFieldName) {
		excelModel = getExcelModel(excelModelClass);
		value = ReflectionUtil.getFieldMap(
			 excelModelClass,
			 field -> {
				 ExcelProperty annotation = field.getAnnotation(ExcelProperty.class);
				 return annotation != null && !isExcludeField(field, excludeFieldName);
			 },
			 field -> new ExcelField(field, ctx)
		);

		if (value.isEmpty()) {
			throw new UnsupportedOperationException(
				 String.format("@ExcelProperty annotation not found in class %s", excelModelClass.getName())
			);
		}
	}

	public static ExcelFields of(Class<?> clazz, ApplicationContext ctx, String... excludeFieldName) {
		return new ExcelFields(clazz, ctx, excludeFieldName);
	}

	public ExcelField getExcelField(int cellColIdx) {
		return value.get(cellColIdx);
	}

	public List<ExcelBatchValidator> getAllBatchValidators() {
		return value.values()
			 .stream()
			 .map(ExcelField::getBatchValidators)
			 .flatMap(Collection::stream)
			 .toList();
	}

	private boolean isExcludeField(Field field, String... excludeFieldName) {
		return excludeFieldName != null && excludeFieldName.length > 0
			 && StringUtils.equalsAny(field.getName(), excludeFieldName);
	}

	private ExcelModel getExcelModel(Class<?> excelModelClass) {
		return Optional.ofNullable(ReflectionUtil.findAnnotation(excelModelClass, ExcelModel.class))
			 .orElseThrow(() -> new UnsupportedOperationException(
				  String.format("@ExcelModel annotation not found in class %s", excelModelClass.getName())
			 ));
	}

	public int size() {
		return value.size();
	}

	public int getTotalTitleRowCount() {
		return excelModel.totalTitleRowCount();
	}

	public String getSheetName() {
		return excelModel.sheetName();
	}

	public Set<Map.Entry<Integer, ExcelField>> entrySet() {
		return value.entrySet();
	}
}
