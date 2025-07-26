package com.gmoon.springpoi.excel.validator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;

import com.gmoon.springpoi.excel.vo.ExcelCell;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ExcelBatchValidator implements ExcelValidator, ExcelFlushable {
	private static final int CHUNK_THRESHOLD = 1_000;

	private final Map<Integer, ExcelCell> buffer = new ConcurrentHashMap<>();

	protected abstract List<String> getInvalidValues(List<String> cellValues);

	public void collect(Integer rowIdx, ExcelCell excelCell) {
		buffer.put(rowIdx, excelCell);
	}

	@Override
	public void flush(Consumer<Map<Integer, ExcelCell>> invalidRowHandler) {
		if (buffer.isEmpty()) {
			return;
		}

		Map<Integer, ExcelCell> invalidRows = getInvalidRows();
		invalidRowHandler.accept(invalidRows);
		buffer.clear();
	}

	private Map<Integer, ExcelCell> getInvalidRows() {
		List<String> invalidValues = getInvalidValues(buffer.values()
			 .stream()
			 .map(ExcelCell::value)
			 .toList());

		Map<Integer, ExcelCell> invalidRows = new LinkedHashMap<>();

		for (Map.Entry<Integer, ExcelCell> entry : buffer.entrySet()) {
			ExcelCell excelCell = entry.getValue();
			String cellValue = excelCell.value();
			if (invalidValues.contains(cellValue)) {
				int rowIdx = entry.getKey();
				invalidRows.put(rowIdx, excelCell);
				log.debug("[excel validation failed] batch validator: {}, invalid rows: {}", this, invalidRows);
			}
		}
		return invalidRows;
	}

	public void flushBufferIfNeeded(Consumer<Map<Integer, ExcelCell>> invalidRowHandler) {
		boolean flushable = buffer.size() >= getChunkThreshold();
		if (flushable && !isBufferValid()) {
			flush(invalidRowHandler);
		}
	}

	private boolean isBufferValid() {
		if (buffer.isEmpty()) {
			return true;
		}

		Map<Integer, ExcelCell> invalidRows = getInvalidRows();
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
