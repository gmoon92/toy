package com.gmoon.springpoi.common.excel.vo;

import java.io.Serial;
import java.io.Serializable;

import com.gmoon.springpoi.excels.domain.ExcelInvalidRowType;
import com.gmoon.springpoi.excels.domain.vo.ExcelCellErrorMessages;
import com.gmoon.springpoi.excels.domain.vo.ExcelCellValues;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(of = "rowIdx", callSuper = false)
public class ExcelInvalidRow implements Serializable {

	@Serial
	private static final long serialVersionUID = 330677365933743453L;

	private final long rowIdx;
	private final ExcelInvalidRowType type;
	private final ExcelCellValues cellValues;
	private final ExcelCellErrorMessages errorMessages;

	public ExcelInvalidRow(long rowIdx, ExcelCellValues cellValues, ExcelInvalidRowType type) {
		this.rowIdx = rowIdx;
		this.type = type;
		this.cellValues = cellValues;
		this.errorMessages = new ExcelCellErrorMessages();
	}

	public void addCell(ExcelCell excelCell) {
		errorMessages.put(excelCell.getColIndex(), excelCell.getErrorMessages());
	}
}
