package com.gmoon.springpoi.common.excel.vo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.gmoon.springpoi.common.excel.validator.ExcelValidationErrorMessage;
import com.gmoon.springpoi.common.excel.validator.ExcelValidator;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(of = "colIndex", callSuper = false)
@Getter
public class ExcelCell {
	private final int colIndex;
	private final String value;
	private final Set<ExcelErrorMessage> errorMessages;

	public ExcelCell(int colIndex, String value, ExcelErrorMessage errorMessages) {
		this.colIndex = colIndex;
		this.value = value;
		this.errorMessages = new HashSet<>();
		this.errorMessages.add(errorMessages);
	}

	public ExcelCell(int colIndex, String value, List<ExcelValidator> failedValidators) {
		this.colIndex = colIndex;
		this.value = value;
		this.errorMessages = failedValidators.stream()
			 .map(ExcelValidationErrorMessage::getErrorMessage)
			 .collect(Collectors.toSet());
	}
}

