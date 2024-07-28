package com.gmoon.dbrecovery.global.recovery.vo;

import com.gmoon.javacore.util.BooleanUtils;
import com.gmoon.javacore.util.StringUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class TableMetadata {

	private final String tableName;
	private final String tableKeyName;
	private final String referenceTableName;
	private final String referenceColumnName;
	private final boolean constraintOnDelete;

	@Builder
	private TableMetadata(String tableName, String tableKeyName, String referenceTableName, String referenceColumnName, int onDelete) {
		this.tableName = tableName;
		this.tableKeyName = tableKeyName;
		this.referenceTableName = referenceTableName;
		this.referenceColumnName = referenceColumnName;
		this.constraintOnDelete = BooleanUtils.toBoolean(onDelete);
	}

	public boolean isReferenceTableOnDelete(TableMetadata target) {
		boolean infinityRecursively = this.equals(target);
		return enableCaseCadeOnDeleteOption()
			 && StringUtils.equals(referenceTableName, target.getTableName())
			 && !infinityRecursively;
	}

	public boolean enableCaseCadeOnDeleteOption() {
		return constraintOnDelete
			 && StringUtils.isNotBlank(referenceTableName);
	}

	public boolean equalsToTable(TableMetadata target) {
		return !enableCaseCadeOnDeleteOption()
			 && tableName.equals(target.tableName);
	}
}
