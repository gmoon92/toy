package com.gmoon.springpoi.common.excel.validator;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;

import com.gmoon.springpoi.common.excel.vo.ExcelCell;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ExcelBatchValidator implements ExcelValidator, ExcelFlushable {
	private static final int CHUNK_THRESHOLD = 1_000;

	private final Map<Long, ExcelCell> buffer = new HashMap<>();

	protected abstract List<String> getInvalidValues(List<String> cellValues);

	public void collect(long rowNum, int cellColIdx, String cellValue) {
		ExcelCell excelCell = Optional.ofNullable(buffer.get(rowNum))
			 .orElseGet(() -> new ExcelCell(cellColIdx, cellValue, getErrorMessage()));

		buffer.put(rowNum, excelCell);
	}

	public void flushBufferIfNeeded(Consumer<Map<Long, ExcelCell>> invalidRowHandler) {
		boolean flushable = buffer.size() >= getChunkThreshold();
		if (flushable && !isBufferValid()) {
			flush(invalidRowHandler);
		}
	}

	/**
	 * @apiNote 청크 사이즈를 변경하려면 구현체에서 이 메서드를 오버라이딩할 것.
	 */
	protected int getChunkThreshold() {
		return CHUNK_THRESHOLD;
	}

	private boolean isBufferValid() {
		if (buffer.isEmpty()) {
			return true;
		}

		Map<Long, ExcelCell> invalidRows = getInvalidRowMap();
		buffer.clear();
		buffer.putAll(invalidRows);
		return invalidRows.isEmpty();
	}

	@Override
	public void flush(Consumer<Map<Long, ExcelCell>> invalidRowHandler) {
		if (buffer.isEmpty()) {
			return;
		}

		Map<Long, ExcelCell> invalidRows = getInvalidRowMap();
		invalidRowHandler.accept(invalidRows);
		buffer.clear();
	}

	private Map<Long, ExcelCell> getInvalidRowMap() {
		List<String> invalidValues = getInvalidValues(getBufferCellValues());

		Map<Long, ExcelCell> invalidRows = new LinkedHashMap<>();
		for (Map.Entry<Long, ExcelCell> entry : buffer.entrySet()) {
			ExcelCell excelCell = entry.getValue();
			String cellValue = excelCell.getValue();
			if (invalidValues.contains(cellValue)) {
				long rowNum = entry.getKey();
				invalidRows.put(rowNum, excelCell);
				log.debug("[excel validation failed] batch validator: {}, invalid rows: {}", this, invalidRows);
			}
		}
		return invalidRows;
	}

	private List<String> getBufferCellValues() {
		return buffer.values()
			 .stream()
			 .map(ExcelCell::getValue)
			 .toList();
	}

	@Override
	public boolean isValid(String cellValue) {
		return StringUtils.isNotBlank(cellValue);
	}
}
