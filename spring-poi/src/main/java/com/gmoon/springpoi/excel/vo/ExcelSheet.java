package com.gmoon.springpoi.excel.vo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ExcelSheet<T> {
	private final Map<Integer, ExcelRow<T>> rows;
	private final Map<Integer, ExcelRow<T>> invalidRows;

	private ExcelSheet(int size) {
		this.rows = LinkedHashMap.newLinkedHashMap(size);
		this.invalidRows = LinkedHashMap.newLinkedHashMap(size);
	}

	public static <T> ExcelSheet<T> create() {
		return new ExcelSheet<>(1_000);
	}

	public static <T> ExcelSheet<T> create(int totalRowCount) {
		return new ExcelSheet<>(totalRowCount);
	}

	public static <T> ExcelSheet<T> empty() {
		return new ExcelSheet<>(0);
	}

	public ExcelRow<T> getRowOrInvalidRow(int rowIdx) {
		return Optional.ofNullable(rows.get(rowIdx))
			 .orElseGet(() -> invalidRows.get(rowIdx));
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
		return rows.size() + invalidRows.size();
	}

	public boolean isValidSheet() {
		return invalidRows.isEmpty();
	}

	public void add(int rowIdx, ExcelRow<T> row) {
		if (row == null) {
			return;
		}

		if (row.isValid()) {
			rows.put(rowIdx, row);
		}
	}

	public void addInvalidRow(int rowIdx, ExcelRow<T> excelRow) {
		excelRow.invalidate();
		invalidRows.put(rowIdx, excelRow);
		rows.remove(rowIdx);
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

	public void remove(int rowIdx) {
		rows.remove(rowIdx);
	}

	public void clearRows() {
		rows.clear();
	}

}
