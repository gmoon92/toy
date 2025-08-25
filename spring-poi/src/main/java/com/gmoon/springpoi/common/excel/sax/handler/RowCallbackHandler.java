package com.gmoon.springpoi.common.excel.sax.handler;

import java.util.Map;
import java.util.Set;

import com.gmoon.springpoi.common.excel.vo.BaseExcelModel;
import com.gmoon.springpoi.common.excel.vo.ExcelInvalidRow;
import com.gmoon.springpoi.common.excel.vo.ExcelRow;
import com.gmoon.springpoi.excels.domain.vo.ExcelCellValues;

@FunctionalInterface
public interface RowCallbackHandler<T extends BaseExcelModel> {
	void handle(
		 Map<Long, ExcelCellValues> originRows,
		 Set<ExcelRow<T>> rows,
		 Set<ExcelInvalidRow> invalidRows
	);
}
