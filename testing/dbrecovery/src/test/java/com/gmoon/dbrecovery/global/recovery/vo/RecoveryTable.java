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
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Slf4j
@Getter
@ToString
public class RecoveryTable {

	private final Map<Table, Set<String>> values;

	private RecoveryTable(final List<TableMetadata> all) {
		values = all.stream()
			 .collect(
				  Collectors.collectingAndThen(
					   toMap(
							Table::from,
							metadata -> obtainDeleteTables(all, metadata),
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

	public static RecoveryTable initialize(List<TableMetadata> metadata) {
		return new RecoveryTable(metadata);
	}

	private Set<String> obtainDeleteTables(List<TableMetadata> metadata, TableMetadata target) {
		return getReferenceTableAllOnDelete(metadata, target)
			 .stream()
			 .map(TableMetadata::getTableName)
			 .collect(Collectors.toSet());
	}

	private Set<TableMetadata> getReferenceTableAllOnDelete(List<TableMetadata> metadata, TableMetadata target) {
		return metadata.stream()
			 .filter(table -> table.isReferenceTableOnDelete(target))
			 .flatMap(table -> {
				 Set<TableMetadata> nestedTables = getReferenceTableAllOnDelete(metadata, table);
				 nestedTables.add(table);
				 return nestedTables.stream();
			 })
			 .collect(Collectors.toSet());
	}

	public Set<String> getDeleteTables(String tableName) {
		return values.get(Table.builder()
			 .name(tableName)
			 .build());
	}

	public Set<Table> getAll() {
		return values.keySet();
	}
}
