package com.gmoon.springpoi.excel.processor;

import com.gmoon.springpoi.excel.vo.ExcelField;
import com.gmoon.springpoi.excel.vo.ExcelRow;
import com.gmoon.springpoi.excel.vo.ExcelSheet;

public interface ExcelCellProcessor<T> {
	void process(ExcelSheet<T> excelSheet, ExcelRow<T> excelRow, ExcelField excelField, String cellValue);
}
