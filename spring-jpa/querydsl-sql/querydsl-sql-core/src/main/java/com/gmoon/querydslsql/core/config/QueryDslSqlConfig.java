package com.gmoon.querydslsql.core.config;

import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.spring.SpringConnectionProvider;
import com.querydsl.sql.spring.SpringExceptionTranslator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * <a href="http://querydsl.com/static/querydsl/latest/reference/html_single/#sql_integration">QueryDSL SQL integration</a>
 */
@Configuration
@RequiredArgsConstructor
public class QueryDslSqlConfig {

	@Bean
	public PlatformTransactionManager jdbcTransactionManager(DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}

	@Bean
	public com.querydsl.sql.Configuration querydslConfiguration() {
		SQLTemplates templates = MySQLTemplates.builder()
			 .build();
		com.querydsl.sql.Configuration configuration = new com.querydsl.sql.Configuration(templates);
		configuration.setExceptionTranslator(new SpringExceptionTranslator());
		return configuration;
	}

	@Bean
	public SQLQueryFactory queryFactory(DataSource dataSource) {
		SpringConnectionProvider provider = new SpringConnectionProvider(dataSource);
		return new SQLQueryFactory(querydslConfiguration(), provider);
	}
}
