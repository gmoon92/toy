package com.gmoon.dbrecovery.global.recovery.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class CaseCadeDeleteTable {

	private final String tableName;

	public CaseCadeDeleteTable(Table table) {
		this.tableName = table.getTableName();
	}
}
