package com.gmoon.springpoi.excel.processor;

import com.gmoon.springpoi.excel.vo.ExcelSheet;

@FunctionalInterface
public interface EventListener {

	void onEvent(ExcelSheet<?> excelSheet);
}
