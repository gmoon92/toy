package com.gmoon.springdatar2dbc.global;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.sql.init.SqlDataSourceScriptDatabaseInitializer;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationProperties;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.sql.DataSource;

@Profile("jpa")
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
