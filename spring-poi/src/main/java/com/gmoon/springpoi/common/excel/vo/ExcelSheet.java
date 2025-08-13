package com.gmoon.springpoi.common.excel.vo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.ApplicationContext;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExcelSheet<T> {

	@Getter
	private final ExcelModelMetadata metadata;

	private final Map<Integer, ExcelRow<T>> rows;
	private final Map<Integer, ExcelCell> invalidRows;

	private final AtomicInteger parsedRowsCount;
	private final AtomicInteger invalidRowsCount;

	public ExcelSheet(ApplicationContext ctx, Class<T> clazz, String... excludeFieldName) {
		this(ctx, clazz, 1_000, excludeFieldName);
	}

	public ExcelSheet(ApplicationContext ctx, Class<T> clazz, int size, String... excludeFieldName) {
		this.metadata = new ExcelModelMetadata(clazz, ctx, excludeFieldName);
		this.parsedRowsCount = new AtomicInteger(0);
		this.invalidRowsCount = new AtomicInteger(0);

		this.rows = HashMap.newHashMap(size);
		this.invalidRows = HashMap.newHashMap(0);
	}

	public ExcelRow<T> createRow(int rowIdx, Class<T> clazz) {
		ExcelRow<T> excelRow = new ExcelRow<>(rowIdx, clazz);
		rows.put(rowIdx, excelRow);
		parsedRowsCount.incrementAndGet();
		return excelRow;
	}

	public List<T> getRows() {
		return rows.entrySet()
			 .stream()
			 .sorted(Map.Entry.comparingByKey())
			 .map(Map.Entry::getValue)
			 .map(ExcelRow::getExcelVO)
			 .toList();
	}

	public List<ExcelCell> getInvalidRows() {
		return invalidRows.entrySet()
			 .stream()
			 .sorted(Map.Entry.comparingByKey())
			 .map(Map.Entry::getValue)
			 .toList();
	}

	public int size() {
		return parsedRowsCount.get();
	}

	public boolean isValidSheet() {
		return invalidRowsCount.get() == 0;
	}

	public void addInvalidRow(int rowIdx, ExcelCell excelCell) {
		if (invalidRows.putIfAbsent(rowIdx, excelCell) == null) {
			invalidRowsCount.incrementAndGet();
		}

		rows.remove(rowIdx);
	}

	public void addInvalidRows(Map<Integer, ExcelCell> invalidChunk) {
		if (invalidChunk.isEmpty()) {
			return;
		}

		for (Map.Entry<Integer, ExcelCell> entry : invalidChunk.entrySet()) {
			int rowIdx = entry.getKey();
			ExcelCell excelCell = entry.getValue();
			addInvalidRow(rowIdx, excelCell);
		}
	}

	public void clearAll() {
		rows.clear();
		invalidRows.clear();
	}
}
