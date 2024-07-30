package com.gmoon.dbrecovery.global.recovery.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class DependentTable {

	private final String tableName;
	private final String tableKeyName;
	private final String tableKeyType;
	private final String referenceTableName;
	private final String referenceColumnName;
	private final String referenceColumnType;

	private DependentTable(TableMetadata metadata) {
		tableName = metadata.getTableName();
		tableKeyName = metadata.getTableKeyName();
		tableKeyType = metadata.getTableKeyType();
		referenceTableName = metadata.getReferenceTableName();
		referenceColumnName = metadata.getReferenceColumnName();
		referenceColumnType = metadata.getReferenceColumnType();
	}

	public static DependentTable from(TableMetadata metadata) {
		return new DependentTable(metadata);
	}
}
