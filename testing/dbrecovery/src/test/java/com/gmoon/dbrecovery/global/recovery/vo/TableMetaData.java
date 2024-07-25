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

	private final Map<Table, Set<CaseCadeDeleteTable>> value;

	private TableMetaData(final List<Table> tables) {
		value = tables.stream()
			 .collect(
				  Collectors.collectingAndThen(
					   toMap(
							Function.identity(),
							table -> obtainDeleteTables(tables, table),
							(existing, replacement) -> {
								Set<CaseCadeDeleteTable> merged = new HashSet<>(existing);
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

	private Set<CaseCadeDeleteTable> obtainDeleteTables(List<Table> tables, Table target) {
		return tables.stream()
			 .filter(table -> table.isReferenceTableOnDelete(target))
			 .map(CaseCadeDeleteTable::new)
			 .collect(Collectors.toSet());
	}

	public Set<CaseCadeDeleteTable> getDeleteTables(Table table) {
		return value.get(table);
	}

	public int getTotalCount() {
		return value.size();
	}
}
