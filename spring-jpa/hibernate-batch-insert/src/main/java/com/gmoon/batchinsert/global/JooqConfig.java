package com.gmoon.batchinsert.global;

import javax.sql.DataSource;

import org.jooq.ConnectionProvider;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.conf.StatementType;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.springframework.boot.autoconfigure.jooq.ExceptionTranslatorExecuteListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import lombok.RequiredArgsConstructor;

/**
 * @see org.springframework.boot.autoconfigure.jooq.JooqAutoConfiguration
 */
@Configuration
@RequiredArgsConstructor
public class JooqConfig {

	private final DataSource dataSource;

	@Bean
	public DSLContext dsl() {
		ConnectionProvider connectionProvider = new DataSourceConnectionProvider(
			 new TransactionAwareDataSourceProxy(dataSource));

		org.jooq.Configuration configuration = configuration(connectionProvider);
		return new DefaultDSLContext(configuration);
	}

	// https://www.jooq.org/doc/latest/manual/sql-building/dsl-context/custom-settings/
	private org.jooq.Configuration configuration(ConnectionProvider connectionProvider) {
		Settings settings = new Settings();
		settings.setStatementType(StatementType.STATIC_STATEMENT);

		DefaultConfiguration config = new DefaultConfiguration();
		config.setConnectionProvider(connectionProvider);
		config.setSQLDialect(SQLDialect.MARIADB);
		config.setSettings(settings);
		config.setExecuteListenerProvider(
			 new DefaultExecuteListenerProvider(ExceptionTranslatorExecuteListener.DEFAULT));
		return config;
	}
}
