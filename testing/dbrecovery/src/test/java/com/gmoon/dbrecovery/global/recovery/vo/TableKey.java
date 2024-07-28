package com.gmoon.dbrecovery.global.recovery.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class TableKey {

	private final String tableName;
	private final String value;

	private TableKey(TableMetadata metadata) {
		tableName = metadata.getTableName();
		value = metadata.getTableKeyName();
	}

	public static TableKey from(TableMetadata metadata) {
		return new TableKey(metadata);
	}
}
