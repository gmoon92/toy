package com.gmoon.commons.commonsapachepoi.excel.processor;

import com.gmoon.commons.commonsapachepoi.excel.vo.ExcelField;
import com.gmoon.commons.commonsapachepoi.excel.vo.ExcelRow;
import com.gmoon.commons.commonsapachepoi.excel.vo.ExcelSheet;

public interface ExcelCellProcessor<T> {
	void process(ExcelSheet<T> excelSheet, ExcelRow<T> excelRow, ExcelField excelField, String cellValue);
}
