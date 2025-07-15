package com.gmoon.commons.commonsapachepoi.excel.vo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

	public void addInvalidData(int rowNum, ExcelRow<T> excelRow) {
		invalidRows.put(rowNum, excelRow);
	}

	public int size() {
		return totalRowNum;
	}

	public List<T> getRows() {
		return rows.values()
			 .stream()
			 .map(ExcelRow::getExcelVO)
			 .collect(Collectors.toList());
	}

	public List<ExcelRow<T>> getInvalidRows() {
		return new ArrayList<>(invalidRows.values());
	}

	public boolean isValidSheet() {
		return invalidRows.isEmpty();
	}

	public void addInvalidRow(Map<ExcelRow<?>, String> invalidChunk) {
		if (invalidChunk.isEmpty()) {
			return;
		}

		for (Map.Entry<ExcelRow<?>, String> entry : invalidChunk.entrySet()) {
			ExcelRow<?> invalidRow = entry.getKey();
			int rowNum = invalidRow.getRowNum();
			rows.remove(rowNum);
			invalidRows.put(invalidRow.getRowNum(), (ExcelRow<T>)invalidRow);
		}
	}
}
