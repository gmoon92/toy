package com.gmoon.springpoi.common.excel.processor;

import java.util.Map;

import com.gmoon.springpoi.common.excel.vo.BaseExcelModel;
import com.gmoon.springpoi.common.excel.vo.ExcelRow;
import com.gmoon.springpoi.excels.domain.vo.ExcelCellValues;

@FunctionalInterface
public interface ExcelRowProcessor<T extends BaseExcelModel> {
	void doProcess(String chunkId, Map<Long, ExcelCellValues> originRows, ExcelRow<T> row);
}
