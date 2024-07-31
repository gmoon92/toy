package com.gmoon.dbrecovery.global.recovery.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class Table {

	private final String tableName;
	private final String tableKeyColumnName;

	public Table(TableMetadata metadata) {
		tableName = metadata.getTableName();
		tableKeyColumnName = metadata.getTableKeyName();
	}
}
