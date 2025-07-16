package com.gmoon.commons.commonsapachepoi.excel.validator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ExcelBatchValidator implements ExcelValidator, ExcelFlushable {
	private static final int CHUNK_THRESHOLD = 1_000;

	private final Map<Integer, String> buffer = new ConcurrentHashMap<>();

	protected abstract List<String> getInvalidValues(List<String> cellValues);

	public void collect(Integer rowNum, String cellValue) {
		buffer.put(rowNum, cellValue);
	}

	@Override
	public void flush(Consumer<Map<Integer, String>> invalidRowHandler) {
		if (buffer.isEmpty()) {
			return;
		}

		Map<Integer, String> invalidRows = getInvalidRows();
		invalidRowHandler.accept(invalidRows);
		buffer.clear();
	}

	private Map<Integer, String> getInvalidRows() {
		List<String> invalidValues = getInvalidValues(new ArrayList<>(buffer.values()));
		Map<Integer, String> invalidRows = new LinkedHashMap<>();
		for (Map.Entry<Integer, String> entry : buffer.entrySet()) {
			String cellValue = entry.getValue();
			if (invalidValues.contains(cellValue)) {
				int rowNum = entry.getKey();
				invalidRows.put(rowNum, cellValue);
				log.debug("[excel validation failed] batch validator: {}, invalid rows: {}", this, invalidRows);
			}
		}
		return invalidRows;
	}

	public boolean flushBufferIfNeeded(Consumer<Map<Integer, String>> invalidRowHandler) {
		boolean flushable = buffer.size() >= getChunkThreshold();
		if (flushable && !isBufferValid()) {
			flush(invalidRowHandler);
			return true;
		}

		return false;
	}

	private boolean isBufferValid() {
		if (buffer.isEmpty()) {
			return true;
		}

		Map<Integer, String> invalidRows = getInvalidRows();
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
