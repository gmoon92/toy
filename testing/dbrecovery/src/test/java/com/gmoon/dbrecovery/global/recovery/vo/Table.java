package com.gmoon.dbrecovery.global.recovery.vo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Table {

	@EqualsAndHashCode.Include
	private final String name;
	private final String keyName;

	public static Table from(TableMetadata metadata) {
		return new Table(metadata.getTableName(), metadata.getTableKeyName());
	}
}
