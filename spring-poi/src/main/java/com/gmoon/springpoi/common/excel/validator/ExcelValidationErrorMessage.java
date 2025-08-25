package com.gmoon.springpoi.common.excel.validator;

import com.gmoon.springpoi.common.excel.vo.ExcelErrorMessage;

@FunctionalInterface
public interface ExcelValidationErrorMessage {
	ExcelErrorMessage getErrorMessage();
}
