package com.gmoon.dbrecovery.global.recovery.vo;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Slf4j
@Getter
@ToString
public class TableMetaData {

	private final Map<Table, Set<String>> value;

	private TableMetaData(final List<Table> tables) {
		value = tables.stream()
			 .collect(
				  Collectors.collectingAndThen(
					   toMap(
							Function.identity(),
							table -> obtainDeleteTables(tables, table),
							(existing, replacement) -> {
								Set<String> merged = new HashSet<>(existing);
								merged.addAll(replacement);
								return Collections.unmodifiableSet(merged);
							},
							HashMap::new
					   ),
					   Collections::unmodifiableMap
				  )
			 );
	}

	public static TableMetaData initialize(List<Table> tables) {
		return new TableMetaData(tables);
	}

	private Set<String> obtainDeleteTables(List<Table> tables, Table target) {
		return getDeleteTablesRecursively(tables, target)
			 .stream()
			 .map(Table::getTableName)
			 .collect(Collectors.toSet());
	}

	private Set<Table> getDeleteTablesRecursively(List<Table> tables, Table target) {
		Set<Table> result = new HashSet<>();
		Set<Table> deleteTables = tables.stream()
			 .filter(table -> table.isReferenceTableOnDelete(target))
			 .collect(Collectors.toSet());

		for (Table table : deleteTables) {
			result.addAll(getDeleteTablesRecursively(tables, table));
		}

		result.addAll(deleteTables);
		return result;
	}

	public Set<String> getDeleteTables(String tableName) {
		return value.get(Table.builder()
			 .tableName(tableName)
			 .build());
	}
}
