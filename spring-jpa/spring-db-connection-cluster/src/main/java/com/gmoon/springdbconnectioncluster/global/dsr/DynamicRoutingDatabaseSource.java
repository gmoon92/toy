package com.gmoon.springdbconnectioncluster.global.dsr;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DynamicRoutingDatabaseSource extends AbstractRoutingDataSource {

	public DynamicRoutingDatabaseSource(DataSourceProperties masterDataSourceProperties, DataSourceProperties slaveDataSourceProperties) {
		DataSource masterDataSource = createDataSource(masterDataSourceProperties);
		DataSource slaveDataSource = createDataSource(slaveDataSourceProperties);
		Map<Object, Object> dataSources = new HashMap<>();
		dataSources.put(TargetDataBase.MASTER, masterDataSource);
		dataSources.put(TargetDataBase.SLAVE, slaveDataSource);

		super.setTargetDataSources(dataSources);
		super.setDefaultTargetDataSource(masterDataSource);
		super.afterPropertiesSet();
	}

	private DataSource createDataSource(DataSourceProperties properties) {
		return properties.initializeDataSourceBuilder()
			.type(HikariDataSource.class)
			.build();
	}

	@Override
	protected Object determineCurrentLookupKey() {
		boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
		log.info("tx readOnly: {}", readOnly);
		if (readOnly) {
			return TargetDataBase.SLAVE;
		}

		return TargetDataBase.MASTER;
	}

	private enum TargetDataBase {
		MASTER, SLAVE
	}
}
