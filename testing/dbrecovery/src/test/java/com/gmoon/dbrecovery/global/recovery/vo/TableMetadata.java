package com.gmoon.dbrecovery.global.recovery.vo;

import com.gmoon.javacore.util.BooleanUtils;
import com.gmoon.javacore.util.StringUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

@Getter
@ToString
public class TableMetadata implements Serializable {

	@Serial
	private static final long serialVersionUID = 8909731597869722097L;

	private final String tableName;
	private final String tableKeyName;
	private final String tableKeyType;
	private final String referenceTableName;
	private final String referenceColumnName;
	private final String referenceColumnType;
	private final boolean constraintOnDelete;

	@Builder
	private TableMetadata(String tableName, String tableKeyName, String tableKeyType,
						  String referenceTableName, String referenceColumnName, String referenceColumnType,
						  int onDelete) {
		this.tableName = tableName;
		this.tableKeyName = tableKeyName;
		this.tableKeyType = tableKeyType;
		this.referenceTableName = referenceTableName;
		this.referenceColumnName = referenceColumnName;
		this.referenceColumnType = referenceColumnType;
		this.constraintOnDelete = BooleanUtils.toBoolean(onDelete);
	}

	public boolean isReferenceTableOnDelete(TableMetadata target) {
		String targetTableName = target.getTableName();
		return enableCaseCadeOnDeleteOption()
			 && StringUtils.equals(referenceTableName, targetTableName)
			 && !StringUtils.equals(tableName, targetTableName);
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
