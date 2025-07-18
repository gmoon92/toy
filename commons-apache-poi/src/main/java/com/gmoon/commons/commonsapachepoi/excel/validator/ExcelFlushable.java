package com.gmoon.commons.commonsapachepoi.excel.validator;

import java.util.Map;
import java.util.function.Consumer;

@FunctionalInterface
public interface ExcelFlushable {
	void flush(Consumer<Map<Integer, String>> invalidRowHandler);
}
