package com.gmoon.springpoi.excel.vo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ExcelSheet<T> {
	private final AtomicInteger parsedRowsCount;

	private final Map<Integer, ExcelRow<T>> rows;
	private final Map<Integer, ExcelRow<T>> invalidRows;

	public ExcelSheet() {
		this(1_000);
	}

	public ExcelSheet(int size) {
		this.parsedRowsCount = new AtomicInteger(0);
		this.rows = LinkedHashMap.newLinkedHashMap(size);
		this.invalidRows = LinkedHashMap.newLinkedHashMap(size);
	}

	public ExcelRow<T> createRow(int rowIdx, Class<T> clazz) {
		ExcelRow<T> excelRow = new ExcelRow<>(rowIdx, clazz);
		rows.put(rowIdx, excelRow);
		parsedRowsCount.incrementAndGet();
		return excelRow;
	}

	public List<T> getRows() {
		return rows.values()
			 .stream()
			 .map(ExcelRow::getExcelVO)
			 .toList();
	}

	public List<T> getInvalidRows() {
		return invalidRows.values()
			 .stream()
			 .map(ExcelRow::getExcelVO)
			 .toList();
	}

	public int size() {
		return parsedRowsCount.get();
	}

	public int getRowSize() {
		return rows.size();
	}

	public boolean isValidSheet() {
		return invalidRows.isEmpty();
	}

	public void addInvalidRow(int rowIdx, ExcelRow<T> excelRow) {
		if (excelRow == null || !excelRow.isValid()) {
			return;
		}

		excelRow.invalidate();
		invalidRows.put(rowIdx, excelRow);
		removeRow(rowIdx);
	}

	private void removeRow(int rowIdx) {
		if (rows.containsKey(rowIdx)) {
			rows.remove(rowIdx);
			parsedRowsCount.decrementAndGet();
		}
	}

	public void addInvalidRows(Map<Integer, String> invalidChunk) {
		if (invalidChunk.isEmpty()) {
			return;
		}

		for (Map.Entry<Integer, String> entry : invalidChunk.entrySet()) {
			int rowIdx = entry.getKey();
			ExcelRow<T> invalidRow = rows.get(rowIdx);
			addInvalidRow(rowIdx, invalidRow);
		}
	}

	public void clearRows() {
		rows.clear();
	}
}
