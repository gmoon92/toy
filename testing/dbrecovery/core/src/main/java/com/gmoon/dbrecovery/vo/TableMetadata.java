package com.gmoon.dbrecovery.vo;

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

	public boolean isReferenceTableOnDelete(TableMetadata target) {
		return !isSelfReferencingTable()
			 && constraintOnDelete
			 && StringUtils.isNotBlank(referenceTableName)
			 && StringUtils.equals(referenceTableName, target.tableName);
	}

	private boolean isSelfReferencingTable() {
		return StringUtils.equals(tableName, referenceTableName);
	}
}
