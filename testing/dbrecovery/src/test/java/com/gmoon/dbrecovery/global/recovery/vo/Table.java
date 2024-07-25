package com.gmoon.dbrecovery.global.recovery.vo;

import com.gmoon.javacore.util.BooleanUtils;
import com.gmoon.javacore.util.StringUtils;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(of = "tableName")
@ToString
public class Table {

	private final String tableName;
	private final String tablePKColumnName;
	private final String referenceTableName;
	private final String referenceTablePKColumnName;
	private final boolean constraintOnDelete;

	@Builder
	private Table(String tableName, String tablePKColumnName, String referenceTableName, String referenceTablePKColumnName, int onDelete) {
		this.tableName = tableName;
		this.tablePKColumnName = tablePKColumnName;
		this.referenceTableName = referenceTableName;
		this.referenceTablePKColumnName = referenceTablePKColumnName;
		this.constraintOnDelete = BooleanUtils.toBoolean(onDelete);
	}

	public boolean isReferenceTableOnDelete(Table target) {
		return enableCaseCadeOnDeleteOption()
			 && StringUtils.equals(referenceTableName, target.getTableName());
	}

	public boolean enableCaseCadeOnDeleteOption() {
		return constraintOnDelete
			 && StringUtils.isNotBlank(referenceTableName);
	}
}
