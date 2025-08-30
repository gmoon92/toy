package com.gmoon.springpoi.excels.domain;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

import com.gmoon.springpoi.common.excel.annotation.ExcelProperty;
import com.gmoon.springpoi.common.excel.handler.ExcelRowHandler;
import com.gmoon.springpoi.common.excel.vo.BaseExcelModel;
import com.gmoon.springpoi.common.utils.DigestUtil;
import com.gmoon.springpoi.common.utils.ReflectionUtil;
import com.gmoon.springpoi.excels.application.UserExcelCreateImporter;
import com.gmoon.springpoi.users.model.ExcelUserVO;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ExcelSheetType implements Serializable {
	USER(ExcelUserVO.class, UserExcelCreateImporter.class);

	private final Class<? extends BaseExcelModel> excelModelClass;
	private final Class<? extends ExcelRowHandler<?>> excelRowProcessorClass;
	private final String signature;

	<T extends BaseExcelModel> ExcelSheetType(
		 Class<T> excelModelClass,
		 Class<? extends ExcelRowHandler<T>> excelRowProcessorClass
	) {
		this.excelModelClass = excelModelClass;
		this.signature = generateSignature(excelModelClass);
		this.excelRowProcessorClass = excelRowProcessorClass;
	}

	private String generateSignature(Class<?> clazz) {
		String filedMetaString = ReflectionUtil.getFieldMap(
				  clazz,
				  field -> field.getAnnotation(ExcelProperty.class) != null,
				  (idx, field) -> {
					  ExcelProperty ann = field.getAnnotation(ExcelProperty.class);
					  boolean required = ann.required();
					  String title = ann.title();
					  String comment = ann.comment();
					  return required + title + comment;
				  }
			 )
			 .entrySet()
			 .stream()
			 .sorted(Comparator.comparingInt(Map.Entry::getKey))
			 .map(Map.Entry::getValue)
			 .collect(Collectors.joining());
		return DigestUtil.sha256(filedMetaString);
	}
}
