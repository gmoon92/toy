package com.gmoon.springpoi.excels.domain.vo;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

import com.gmoon.springpoi.common.utils.JsonUtil;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
@Setter
public class ExcelCellValues implements Serializable {

	@Serial
	private static final long serialVersionUID = -6828436071974441655L;

	private Map<Integer, String> values = new HashMap<>();

	public ExcelCellValues(Map<Integer, String> values) {
		this.values = values;
	}

	public ExcelCellValues(String jsonString) {
		if (jsonString == null || jsonString.isEmpty()) {
			values = new HashMap<>();
		} else {
			values = JsonUtil.toMap(jsonString, new TypeReference<>() {
			});
		}
	}

	public boolean isEmpty() {
		return values == null || values.isEmpty();
	}

	public String get(int key) {
		return values.get(key);
	}

	public void put(int key, String value) {
		values.put(key, value);
	}

	public String toJsonString() {
		if (values == null || values.isEmpty()) {
			return null;
		}
		return JsonUtil.toString(values);
	}
}
