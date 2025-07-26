package com.gmoon.springpoi.excel.validator;

import java.util.Map;
import java.util.function.Consumer;

import com.gmoon.springpoi.excel.vo.ExcelCell;

@FunctionalInterface
public interface ExcelFlushable {
	void flush(Consumer<Map<Integer, ExcelCell>> invalidRowHandler);
}
