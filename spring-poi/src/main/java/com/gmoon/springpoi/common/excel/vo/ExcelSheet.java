package com.gmoon.springpoi.common.excel.vo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.context.ApplicationContext;

import com.gmoon.springpoi.excels.domain.ExcelInvalidRowType;
import com.gmoon.springpoi.excels.domain.vo.ExcelCellValues;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExcelSheet<T extends BaseExcelModel> {
	private final Class<T> excelModelClass;

	@Getter
	private final ExcelModelMetadata metadata;

	@Getter
	private final Map<Long, ExcelCellValues> originRows;
	private final Map<Long, ExcelRow<T>> rows;
	private final Map<Long, ExcelInvalidRow> invalidRows;

	public ExcelSheet(ApplicationContext ctx, Class<T> clazz, String... excludeFieldName) {
		this(ctx, clazz, 1_000, excludeFieldName);
	}

	public ExcelSheet(ApplicationContext ctx, Class<T> clazz, int size, String... excludeFieldName) {
		this.excelModelClass = clazz;
		this.metadata = new ExcelModelMetadata(clazz, ctx, excludeFieldName);

		this.originRows = HashMap.newHashMap(size);
		this.rows = HashMap.newHashMap(0);
		this.invalidRows = HashMap.newHashMap(0);
	}

	public ExcelRow<T> createRow(long rowIdx) {
		ExcelRow<T> excelRow = new ExcelRow<>(rowIdx, excelModelClass);
		rows.put(rowIdx, excelRow);
		return excelRow;
	}

	public void addOriginValue(long rowIdx, int cellColIdx, String cellValues) {
		ExcelCellValues excelCellValues = originRows.get(rowIdx);
		if (excelCellValues == null) {
			excelCellValues = new ExcelCellValues();
		}

		excelCellValues.put(cellColIdx, cellValues);
		originRows.put(rowIdx, excelCellValues);
	}

	public Set<ExcelRow<T>> getRows() {
		return new HashSet<>(new ArrayList<>(rows.values()));
	}

	public Set<ExcelInvalidRow> getInvalidRows() {
		return new HashSet<>(new ArrayList<>(invalidRows.values()));
	}

	public void addInvalidRows(Map<Long, ExcelCell> invalidRows) {
		if (invalidRows.isEmpty()) {
			return;
		}

		for (Map.Entry<Long, ExcelCell> entry : invalidRows.entrySet()) {
			long rowIdx = entry.getKey();
			addInvalidRow(rowIdx, entry.getValue());
		}
	}

	public void addInvalidRow(long rowIdx, ExcelCell excelCell) {
		ExcelInvalidRow invalidRow = invalidRows.get(rowIdx);
		boolean firstInvalid = invalidRow == null;
		if (firstInvalid) {
			ExcelCellValues original = originRows.get(rowIdx);
			invalidRow = new ExcelInvalidRow(rowIdx, original, ExcelInvalidRowType.VALIDATION);
		}

		invalidRow.addCell(excelCell);
		invalidRows.put(rowIdx, invalidRow);

		remove(rowIdx);
	}

	private void remove(long rowIdx) {
		rows.remove(rowIdx);
	}
}
