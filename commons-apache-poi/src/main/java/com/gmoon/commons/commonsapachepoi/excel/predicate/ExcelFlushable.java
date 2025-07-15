package com.gmoon.commons.commonsapachepoi.excel.predicate;

import com.gmoon.commons.commonsapachepoi.excel.vo.ExcelSheet;

@FunctionalInterface
public interface ExcelFlushable {
	void flush(ExcelSheet<?> excelSheet);
}
