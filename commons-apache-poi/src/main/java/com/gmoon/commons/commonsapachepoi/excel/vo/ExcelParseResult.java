package com.gmoon.commons.commonsapachepoi.excel.vo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
public class ExcelParseResult<T> {
	private final Map<Integer, T> data;
	private final Map<Integer, InvalidExcelRow<T>> invalidRows;
	private final int totalRowNum;
	private final int totalCellNum;

	public static <T> ExcelParseResult<T> create(int totalRowNum, int totalCellNum) {
		return new ExcelParseResult<>(
			 new LinkedHashMap<>(totalRowNum),
			 new LinkedHashMap<>(),
			 totalRowNum,
			 totalCellNum
		);
	}

	public static <T> ExcelParseResult<T> empty() {
		return ExcelParseResult.create(0, 0);
	}

	public void add(int rowNum, T excelModel) {
		data.put(rowNum, excelModel);
	}

	public void addInvalidData(int rowNum, T excelModel, String fieldName, String cellValue) {
		InvalidExcelRow<T> row = Optional.ofNullable(invalidRows.get(rowNum))
			 .orElseGet(() -> new InvalidExcelRow<>(totalCellNum));

		invalidRows.put(rowNum, row.add(fieldName, cellValue, excelModel));
	}

	public int size() {
		return totalRowNum;
	}

	public List<T> getData() {
		return new ArrayList<>(data.values());
	}

	public List<InvalidExcelRow<T>> getInvalidRows() {
		return new ArrayList<>(invalidRows.values());
	}

	@RequiredArgsConstructor
	@ToString
	public static class InvalidExcelRow<T> {

		private final Map<String, String> cells;
		private T data;

		private InvalidExcelRow(int totalCellNum) {
			this.cells = LinkedHashMap.newLinkedHashMap(totalCellNum);
		}

		private InvalidExcelRow<T> add(String fieldName, String cellValue, T data) {
			this.cells.put(fieldName, cellValue);
			this.data = data;
			return this;
		}
	}
}
