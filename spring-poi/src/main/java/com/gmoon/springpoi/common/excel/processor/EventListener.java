package com.gmoon.springpoi.common.excel.processor;

import com.gmoon.springpoi.common.excel.vo.ExcelSheet;

@FunctionalInterface
public interface EventListener {

	void onEvent(ExcelSheet<?> excelSheet);
}
