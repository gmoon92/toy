package com.gmoon.springpoi.common.excel.handler;

import java.util.Map;

import com.gmoon.springpoi.common.excel.vo.BaseExcelModel;
import com.gmoon.springpoi.common.excel.vo.ExcelRow;
import com.gmoon.springpoi.excels.domain.vo.ExcelCellValues;

@FunctionalInterface
public interface ExcelRowHandler<T extends BaseExcelModel> {
	void handle(String chunkId, Map<Long, ExcelCellValues> originRows, ExcelRow<T> row);
}
