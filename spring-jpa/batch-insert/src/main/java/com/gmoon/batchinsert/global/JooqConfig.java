package com.gmoon.batchinsert.global;

import javax.sql.DataSource;

import org.jooq.ConnectionProvider;
import org.jooq.DSLContext;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.springframework.boot.autoconfigure.jooq.JooqExceptionTranslator;
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

	private org.jooq.Configuration configuration(ConnectionProvider connectionProvider) {
		DefaultConfiguration jooqConfiguration = new DefaultConfiguration();
		jooqConfiguration.set(connectionProvider);
		jooqConfiguration.set(new DefaultExecuteListenerProvider(new JooqExceptionTranslator()));
		return jooqConfiguration;
	}
}
