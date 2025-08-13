package com.gmoon.springpoi.common.excel.validator;

import java.util.Map;
import java.util.function.Consumer;

import com.gmoon.springpoi.common.excel.vo.ExcelCell;

@FunctionalInterface
public interface ExcelFlushable {
	void flush(Consumer<Map<Integer, ExcelCell>> invalidRowHandler);
}
