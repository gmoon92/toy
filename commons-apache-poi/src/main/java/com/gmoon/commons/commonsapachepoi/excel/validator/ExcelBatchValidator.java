package com.gmoon.commons.commonsapachepoi.excel.validator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import com.gmoon.commons.commonsapachepoi.excel.vo.ExcelRow;
import com.gmoon.commons.commonsapachepoi.excel.vo.ExcelSheet;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ExcelBatchValidator implements ExcelValidator, ExcelFlushable {
	private static final int CHUNK_THRESHOLD = 1_000;

	private final Map<ExcelRow<?>, String> buffer = new ConcurrentHashMap<>();

	protected abstract List<String> getInvalidValues(List<String> cellValues);

	public <T> void collect(ExcelRow<T> excelRow, String cellValue) {
		buffer.put(excelRow, cellValue);
	}

	@Override
	public void flush(ExcelSheet<?> excelSheet) {
		if (buffer.isEmpty()) {
			return;
		}

		Map<ExcelRow<?>, String> invalidRows = getInvalidRows();
		excelSheet.addInvalidRows(invalidRows);
		buffer.clear();
	}

	private Map<ExcelRow<?>, String> getInvalidRows() {
		List<String> invalidValues = getInvalidValues(new ArrayList<>(buffer.values()));
		Map<ExcelRow<?>, String> invalidRows = new LinkedHashMap<>();
		for (Map.Entry<ExcelRow<?>, String> entry : buffer.entrySet()) {
			String cellValue = entry.getValue();
			if (invalidValues.contains(cellValue)) {
				ExcelRow<?> excelRow = entry.getKey();
				invalidRows.put(excelRow, cellValue);
				log.debug("[excel validation failed] batch validator: {}, invalid rows: {}", this, invalidRows);
			}
		}
		return invalidRows;
	}

	public boolean flushBufferIfNeeded(ExcelSheet<?> excelSheet) {
		boolean flushable = buffer.size() >= getChunkThreshold();
		if (flushable && !isBufferValid()) {
			flush(excelSheet);
			return true;
		}

		return false;
	}

	private boolean isBufferValid() {
		if (buffer.isEmpty()) {
			return true;
		}

		Map<ExcelRow<?>, String> invalidRows = getInvalidRows();
		buffer.clear();
		buffer.putAll(invalidRows);
		return invalidRows.isEmpty();
	}

	protected int getChunkThreshold() {
		return CHUNK_THRESHOLD;
	}

	@Override
	public boolean isValid(String cellValue) {
		return StringUtils.isNotBlank(cellValue);
	}
}
