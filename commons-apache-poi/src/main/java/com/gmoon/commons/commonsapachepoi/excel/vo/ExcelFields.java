package com.gmoon.commons.commonsapachepoi.excel.vo;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.gmoon.commons.commonsapachepoi.common.utils.ReflectionUtil;
import com.gmoon.commons.commonsapachepoi.excel.annotation.ExcelModel;
import com.gmoon.commons.commonsapachepoi.excel.annotation.ExcelProperty;
import com.gmoon.commons.commonsapachepoi.excel.validator.ExcelBatchValidator;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;

@Getter
public class ExcelFields {
	private final ExcelModel excelModel;
	private final Map<Integer, ExcelField> value;

	private ExcelFields(Class<?> excelModelClass, ApplicationContext ctx, String... excludeFieldNames) {
		excelModel = getExcelModel(excelModelClass);

		Map<Integer, ExcelField> result = new HashMap<>();
		int fieldNum = 0;
		Field[] fields = excelModelClass.getDeclaredFields();
		for (Field field : fields) {
			ExcelProperty annotation = field.getAnnotation(ExcelProperty.class);
			if (annotation == null || isExcludeField(field, excludeFieldNames)) {
				continue;
			}

			field.setAccessible(true);
			result.put(fieldNum++, new ExcelField(field, annotation, ctx));
		}

		if (CollectionUtils.isEmpty(result.values())) {
			throw new UnsupportedOperationException(
				 String.format("@ExcelProperty annotation not found in class %s", excelModelClass.getName())
			);
		}

		value = Collections.unmodifiableMap(result);
	}

	public static ExcelFields of(Class<?> clazz, HttpServletRequest request, String... excludeFieldNames) {
		ServletContext servletContext = request.getServletContext();
		ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);

		return new ExcelFields(clazz, ctx, excludeFieldNames);
	}

	public List<ExcelBatchValidator> getAllBatchValidators() {
		return value.values()
			 .stream()
			 .flatMap(excelField -> excelField.getBatchValidators().stream())
			 .collect(Collectors.toList());
	}

	private boolean isExcludeField(Field field, String... excludeFieldNames) {
		return excludeFieldNames != null && excludeFieldNames.length > 0
			 && StringUtils.equalsAny(field.getName(), excludeFieldNames);
	}

	private ExcelModel getExcelModel(Class<?> excelModelClass) {
		return Optional.ofNullable(ReflectionUtil.findAnnotation(excelModelClass, ExcelModel.class))
			 .orElseThrow(() -> new UnsupportedOperationException(
				  String.format("@ExcelAutoDetect annotation not found in class %s", excelModelClass.getName())
			 ));
	}

	public int size() {
		return value.size();
	}

	public Set<Map.Entry<Integer, ExcelField>> entrySet() {
		return value.entrySet();
	}

}
