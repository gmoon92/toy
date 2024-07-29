package com.gmoon.dbrecovery.global.recovery;

import com.gmoon.dbrecovery.global.recovery.properties.RecoveryDatabaseProperties;
import com.gmoon.dbrecovery.global.recovery.vo.DependentTable;
import com.gmoon.dbrecovery.global.recovery.vo.TableKey;
import com.gmoon.dbrecovery.global.recovery.vo.TableMetadata;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Slf4j
@Component
@RequiredArgsConstructor
@ToString
public class RecoveryTable implements InitializingBean {

	private final DataSource dataSource;
	private final RecoveryDatabaseProperties properties;

	private Map<String, Set<TableKey>> tables;
	private Map<String, Set<DependentTable>> dependentTables;

	@Override
	public void afterPropertiesSet() throws Exception {
		List<TableMetadata> tableMetadata = getTableMetadata();
		initialize(tableMetadata);
	}

	private List<TableMetadata> getTableMetadata() {
		String queryString =
			 "SELECT kcu.TABLE_NAME             AS table_name, " +
				  "  kcu.COLUMN_NAME            AS table_key_column_name, " +
				  "  kcu.REFERENCED_TABLE_NAME  AS ref_table_name, " +
				  "  kcu.REFERENCED_COLUMN_NAME AS ref_column_name, " +
				  "  CASE WHEN rc.DELETE_RULE = 'CASCADE' " +
				  "      THEN 1 " +
				  "      ELSE 0 " +
				  "      END AS on_delete " +
				  "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE kcu " +
				  "         LEFT JOIN INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS rc " +
				  "              ON kcu.CONSTRAINT_NAME = rc.CONSTRAINT_NAME " +
				  "                  AND kcu.CONSTRAINT_SCHEMA = rc.CONSTRAINT_SCHEMA " +
				  "WHERE kcu.TABLE_SCHEMA = ? ";

		try (Connection connection = dataSource.getConnection();
			 PreparedStatement statement = connection.prepareStatement(queryString)) {
			statement.setString(1, properties.getSchema());
			statement.execute();

			ResultSet resultSet = statement.getResultSet();
			List<TableMetadata> metadata = new ArrayList<>();
			while (resultSet.next()) {
				metadata.add(TableMetadata.builder()
					 .tableName(resultSet.getString("table_name"))
					 .tableKeyName(resultSet.getString("table_key_column_name"))
					 .referenceTableName(resultSet.getString("ref_table_name"))
					 .referenceColumnName(resultSet.getString("ref_column_name"))
					 .onDelete(resultSet.getInt("on_delete"))
					 .build());
			}
			return metadata;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void initialize(List<TableMetadata> all) {
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
