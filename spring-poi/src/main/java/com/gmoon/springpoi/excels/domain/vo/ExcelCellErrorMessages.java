package com.gmoon.springpoi.excels.domain.vo;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;

import com.gmoon.springpoi.common.excel.vo.ExcelErrorMessage;
import com.gmoon.springpoi.common.utils.JsonUtil;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = false)
@ToString
public class ExcelCellErrorMessages implements Serializable {

	@Serial
	private static final long serialVersionUID = -8054549926259981795L;
	private Map<Integer, Set<ExcelErrorMessage>> values = new HashMap<>();

	public ExcelCellErrorMessages(String jsonString) {
		if (jsonString == null || jsonString.isEmpty()) {
			values = new HashMap<>();
		} else {
			values = JsonUtil.toMap(
				 jsonString,
				 new TypeReference<Map<Integer, Set<ExcelErrorMessage>>>() {
				 }
			);
		}
	}

	public Set<Map.Entry<Integer, Set<ExcelErrorMessage>>> entrySet() {
		return values.entrySet();
	}

	public boolean isEmpty() {
		return values == null || values.isEmpty();
	}

	public Set<ExcelErrorMessage> get(Integer key) {
		return values.get(key);
	}

	public void put(Integer key, Set<ExcelErrorMessage> value) {
		Set<ExcelErrorMessage> messages = Optional.ofNullable(values.get(key))
			 .orElseGet(HashSet::new);
		messages.addAll(value);

		values.put(key, messages);
	}

	public String toJsonString() {
		if (isEmpty()) {
			return null;
		}

		return JsonUtil.toString(values);
	}
}
