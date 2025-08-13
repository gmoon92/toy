package com.gmoon.springpoi.common.excel.processor;

import com.gmoon.springpoi.common.excel.annotation.ExcelComponent;
import com.gmoon.springpoi.common.excel.vo.ExcelSheet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExcelComponent
@RequiredArgsConstructor
public class SaxRowEventListener implements EventListener {

	@Override
	public void onEvent(ExcelSheet<?> excelSheet) {
		excelSheet.clearAll();
	}
}
