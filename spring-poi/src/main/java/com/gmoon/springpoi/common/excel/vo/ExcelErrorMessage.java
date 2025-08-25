package com.gmoon.springpoi.common.excel.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ExcelErrorMessage {

	private String resourceKey;
	private Object[] args;

	public ExcelErrorMessage(String resourceKey, Object... args) {
		this.resourceKey = resourceKey;
		this.args = args;
	}
}
