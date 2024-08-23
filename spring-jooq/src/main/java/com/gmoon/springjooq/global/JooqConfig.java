package com.gmoon.springjooq.global;

import javax.sql.DataSource;

import org.jooq.ConnectionProvider;
import org.jooq.SQLDialect;
import org.jooq.codegen.GenerationTool;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.jooq.meta.jaxb.Database;
import org.jooq.meta.jaxb.Generator;
import org.jooq.meta.jaxb.Jdbc;
import org.jooq.meta.jaxb.Target;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.jooq.ExceptionTranslatorExecuteListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

/**
 * @see org.springframework.boot.autoconfigure.jooq.JooqAutoConfiguration
 */
@Configuration
@RequiredArgsConstructor
public class JooqConfig {

	private final DataSource dataSource;
	private final Environment environment;
	private final DataSourceProperties dataSourceProperties;

	/**
	 * @link {https://www.jooq.org/doc/latest/manual/code-generation/codegen-programmatic/}
	 */
	@PostConstruct
	public void generateSource() throws Exception {
		org.jooq.meta.jaxb.Configuration configuration = new org.jooq.meta.jaxb.Configuration()
			 .withJdbc(new Jdbc()
				  .withDriver(dataSourceProperties.getDriverClassName())
				  .withUrl(dataSourceProperties.getUrl())
				  .withUser(dataSourceProperties.getUsername())
				  .withPassword(dataSourceProperties.getPassword()))

			 .withGenerator(new Generator()
				  .withDatabase(new Database()
					   .withSchemaVersionProvider("SELECT 'V1' AS version") // todo make custom schema version provider.
					   .withName(
							environment.getProperty("jooq.properties.driver-class-name")) // org.jooq.meta.*.*Database
					   // .withIncludes(".*")
					   // .withExcludes("")
					   .withInputSchema(environment.getProperty("jooq.properties.schema"))
				  )
				  .withTarget(
					   new Target()
							.withDirectory(environment.getProperty("jooq.properties.target-dir"))
							.withPackageName("com.gmoon.springjooq.global.jooqschema")
				  )
			 );
		GenerationTool.generate(configuration);
	}

	@Bean
	public ConnectionProvider connectionProvider() {
		return new DataSourceConnectionProvider(new TransactionAwareDataSourceProxy(dataSource));
	}

	@Bean
	public DefaultDSLContext dsl(ConnectionProvider connectionProvider) {
		org.jooq.Configuration configuration = configuration(connectionProvider);
		return new DefaultDSLContext(configuration);
	}

	private org.jooq.Configuration configuration(ConnectionProvider connectionProvider) {
		DefaultConfiguration config = new DefaultConfiguration();
		config.setConnectionProvider(connectionProvider);
		config.setExecuteListenerProvider(
			 new DefaultExecuteListenerProvider(ExceptionTranslatorExecuteListener.DEFAULT));
		config.setSQLDialect(SQLDialect.valueOf(environment.getProperty("jooq.properties.direct")));
		return config;
	}
}
