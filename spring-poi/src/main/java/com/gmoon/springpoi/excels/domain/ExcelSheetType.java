package com.gmoon.springpoi.excels.domain;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

import com.gmoon.springpoi.common.excel.annotation.ExcelProperty;
import com.gmoon.springpoi.common.utils.DigestUtil;
import com.gmoon.springpoi.common.utils.ReflectionUtil;
import com.gmoon.springpoi.users.model.ExcelUserVO;

public enum ExcelSheetType {
	USER(ExcelUserVO.class);

	public final String signature;

	ExcelSheetType(Class<?> clazz) {
		this.signature = generateSignature(clazz);
	}

	private String generateSignature(Class<?> clazz) {
		String filedMetaString = ReflectionUtil.getFieldMap(
				  clazz,
				  field -> field.getAnnotation(ExcelProperty.class) != null,
				  field -> {
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
