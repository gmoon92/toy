package com.gmoon.dbrecovery.global.recovery.vo;

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
@ToString
public class RecoveryTable {

	private final Map<String, Set<TableKey>> tables;
	private final Map<String, Set<DependentTable>> dependentTables;

	public RecoveryTable(List<TableMetadata> all) {
		tables = all.stream()
			 .collect(
				  Collectors.collectingAndThen(
					   toMap(
							TableMetadata::getTableName,
							metadata -> all.stream()
								 .filter(metadata::equalsToTable)
								 .map(TableKey::from)
								 .collect(Collectors.toSet()),
							(existing, replacement) -> {
								Set<TableKey> merged = new HashSet<>(existing);
								merged.addAll(replacement);
								return Collections.unmodifiableSet(merged);
							},
							HashMap::new
					   ),
					   Collections::unmodifiableMap
				  )
			 );

		dependentTables = all.stream()
			 .collect(
				  Collectors.collectingAndThen(
					   toMap(
							TableMetadata::getTableName,
							metadata -> obtainDeleteTables(all, metadata),
							(existing, replacement) -> {
								Set<DependentTable> merged = new HashSet<>(existing);
								merged.addAll(replacement);
								return Collections.unmodifiableSet(merged);
							},
							HashMap::new
					   ),
					   Collections::unmodifiableMap
				  )
			 );
	}

	public static RecoveryTable initialize(List<TableMetadata> all) {
		return new RecoveryTable(all);
	}

	private Set<DependentTable> obtainDeleteTables(List<TableMetadata> metadata, TableMetadata target) {
		return getReferenceTableAllOnDelete(metadata, target)
			 .stream()
			 .map(DependentTable::from)
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
		Set<DependentTable> result = dependentTables.getOrDefault(tableName, new HashSet<>());
		return result.stream()
			 .map(DependentTable::getTableName)
			 .collect(Collectors.toSet());
	}

	public Set<String> getTableAll() {
		return tables.keySet();
	}
}
