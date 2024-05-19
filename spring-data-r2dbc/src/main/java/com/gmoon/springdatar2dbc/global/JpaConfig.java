package com.gmoon.springdatar2dbc.global;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.sql.init.SqlDataSourceScriptDatabaseInitializer;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableJpaRepositories(
	 basePackages = {"com.gmoon.*"},
	 excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, value = ReactRepository.class)
)
public class JpaConfig {

	@Bean
	public DataSource jdbcDataSource(DataSourceProperties properties) {
		return properties.initializeDataSourceBuilder()
			 .type(HikariDataSource.class)
			 .build();
	}

	/**
	 * {@link org.springframework.boot.autoconfigure.sql.init.DataSourceInitializationConfiguration}
	 * {@link org.springframework.boot.autoconfigure.sql.init.R2dbcInitializationConfiguration}
	 * */
	@Bean
	public SqlDataSourceScriptDatabaseInitializer dataSourceScriptDatabaseInitializer(
		 DataSource jdbcDataSource,
		 SqlInitializationProperties properties
	) {
		return new SqlDataSourceScriptDatabaseInitializer(jdbcDataSource, properties);
	}
}
