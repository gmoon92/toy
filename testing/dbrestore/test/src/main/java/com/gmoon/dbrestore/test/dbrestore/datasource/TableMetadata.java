package com.gmoon.dbrestore.test.dbrestore.datasource;

import com.gmoon.javacore.util.BooleanUtils;
import com.gmoon.javacore.util.StringUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
class TableMetadata {

	private final String tableName;
	private final String tableKeyName;
	private final String referenceTableName;
	private final String referenceColumnName;
	private final boolean constraintOnDelete;

	@Builder
	private TableMetadata(String tableName, String tableKeyName,
						  String referenceTableName, String referenceColumnName,
						  int onDelete
	) {
		this.tableName = tableName;
		this.tableKeyName = tableKeyName;
		this.referenceTableName = referenceTableName;
		this.referenceColumnName = referenceColumnName;
		this.constraintOnDelete = BooleanUtils.toBoolean(onDelete);
	}

	public boolean isOnDeleteCascadeReferenceTable(TableMetadata target) {
		return constraintOnDelete
			 && StringUtils.isNotBlank(referenceTableName)
			 && !isSelfReferencingTable()
			 && StringUtils.equals(referenceTableName, target.tableName);
	}

	private boolean isSelfReferencingTable() {
		return StringUtils.equals(tableName, referenceTableName);
	}
}
