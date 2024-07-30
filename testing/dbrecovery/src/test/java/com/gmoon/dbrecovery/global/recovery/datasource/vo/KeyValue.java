package com.gmoon.dbrecovery.global.recovery.datasource.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class KeyValue {

	@EqualsAndHashCode.Include
	private final String keyColumnName;
	private final String value;
}
