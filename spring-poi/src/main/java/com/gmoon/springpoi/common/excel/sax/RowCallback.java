package com.gmoon.springpoi.common.excel.sax;

import java.util.Map;
import java.util.Set;

import com.gmoon.springpoi.common.excel.vo.BaseExcelModel;
import com.gmoon.springpoi.common.excel.vo.ExcelInvalidRow;
import com.gmoon.springpoi.common.excel.vo.ExcelRow;
import com.gmoon.springpoi.excels.domain.vo.ExcelCellValues;

@FunctionalInterface
public interface RowCallback<T extends BaseExcelModel> {
	void accept(
		 Map<Long, ExcelCellValues> originRows,
		 Set<ExcelRow<T>> rows,
		 Set<ExcelInvalidRow> invalidRows
	);
}
