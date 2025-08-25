package com.gmoon.springpoi.common.excel.processor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.gmoon.springpoi.common.excel.vo.BaseExcelModel;
import com.gmoon.springpoi.common.excel.vo.ExcelInvalidRow;
import com.gmoon.springpoi.common.excel.vo.ExcelRow;
import com.gmoon.springpoi.excels.application.ExcelUploadTaskService;
import com.gmoon.springpoi.excels.domain.ExcelInvalidRowType;
import com.gmoon.springpoi.excels.domain.vo.ExcelCellValues;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractExcelRowProcessor<T extends BaseExcelModel> implements ExcelRowProcessor<T> {

	@Autowired
	private ExcelUploadTaskService taskService;

	@Override
	public final void doProcess(String chunkId, Map<Long, ExcelCellValues> originRows, ExcelRow<T> row) {
		Set<ExcelInvalidRow> invalidRows = new HashSet<>();

		try {
			T excelVO = row.getExcelVO();
			doProcess(excelVO);
		} catch (Exception e) {
			long rowIdx = row.getRowIdx();
			log.warn("Failed to process Excel row {} due to a business rule violation or system error.", rowIdx, e);

			ExcelCellValues excelCellValues = originRows.get(rowIdx);
			invalidRows.add(new ExcelInvalidRow(rowIdx, excelCellValues, ExcelInvalidRowType.BUSINESS));
		}

		taskService.saveInvalidRows(chunkId, invalidRows);
	}

	protected abstract void doProcess(T excelVO);
}
