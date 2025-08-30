package com.gmoon.springpoi.common.excel.handler;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.springpoi.common.excel.vo.BaseExcelModel;
import com.gmoon.springpoi.common.excel.vo.ExcelInvalidRow;
import com.gmoon.springpoi.common.excel.vo.ExcelRow;
import com.gmoon.springpoi.excels.application.ExcelUploadTaskService;
import com.gmoon.springpoi.excels.domain.ExcelInvalidRowType;
import com.gmoon.springpoi.excels.domain.vo.ExcelCellValues;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractExcelRowHandler<T extends BaseExcelModel> implements ExcelRowHandler<T> {

	@Autowired
	private ExcelUploadTaskService taskService;

	protected abstract void applyRow(T excelVO);

	@Transactional
	@Override
	public void handle(String chunkId, Map<Long, ExcelCellValues> originRows, ExcelRow<T> row) {
		Set<ExcelInvalidRow> invalidRows = new HashSet<>();

		try {
			T excelVO = row.getExcelVO();
			applyRow(excelVO);
		} catch (Exception e) {
			long rowIdx = row.getRowIdx();
			log.warn("Failed to process Excel row {} due to a business rule violation or system error.", rowIdx, e);

			ExcelCellValues excelCellValues = originRows.get(rowIdx);
			invalidRows.add(new ExcelInvalidRow(rowIdx, excelCellValues, ExcelInvalidRowType.BUSINESS));
		}

		if (!invalidRows.isEmpty()) {
			taskService.saveInvalidRows(chunkId, invalidRows);
		}
	}
}
