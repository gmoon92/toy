package com.gmoon.commons.commonsapachepoi.excel.vo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExcelSheet<T> {
	private final Map<Integer, ExcelRow<T>> rows;
	private final Map<Integer, ExcelRow<T>> invalidRows;
	private final int totalRowNum;

	public static <T> ExcelSheet<T> create(int totalRowNum) {
		return new ExcelSheet<>(
			 new LinkedHashMap<>(totalRowNum),
			 new LinkedHashMap<>(),
			 totalRowNum
		);
	}

	public static <T> ExcelSheet<T> empty() {
		return ExcelSheet.create(0);
	}

	public void add(int rowNum, ExcelRow<T> row) {
		rows.put(rowNum, row);
	}

	public void addInvalidRow(int rowNum, ExcelRow<T> excelRow) {
		invalidRows.put(rowNum, excelRow);
	}

	public void addInvalidRows(Map<Integer, String> invalidChunk) {
		if (invalidChunk.isEmpty()) {
			return;
		}

		for (Map.Entry<Integer, String> entry : invalidChunk.entrySet()) {
			int rowNum = entry.getKey();
			ExcelRow<T> invalidRow = rows.get(rowNum);
			invalidRows.put(rowNum, invalidRow);

			rows.remove(rowNum);
		}
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
		return totalRowNum;
	}

	public boolean isValidSheet() {
		return invalidRows.isEmpty();
	}
}
