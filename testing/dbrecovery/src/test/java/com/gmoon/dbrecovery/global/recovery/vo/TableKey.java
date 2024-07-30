package com.gmoon.dbrecovery.global.recovery.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class TableKey {

	private final String tableName;
	private final String keyType;
	private final String keyValue;

	private TableKey(TableMetadata metadata) {
		tableName = metadata.getTableName();
		keyValue = metadata.getTableKeyName();
		keyType = metadata.getTableKeyType();
	}

	public static TableKey from(TableMetadata metadata) {
		return new TableKey(metadata);
	}
}
