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
	private final String referenceTableName;
	private final String referenceColumnName;

	private DependentTable(TableMetadata metadata) {
		tableName = metadata.getTableName();
		tableKeyName = metadata.getTableKeyName();
		referenceTableName = metadata.getReferenceTableName();
		referenceColumnName = metadata.getReferenceColumnName();
	}

	public static DependentTable from(TableMetadata metadata) {
		return new DependentTable(metadata);
	}
}
