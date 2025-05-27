package com.gmoon.batchinsert.global.config.jooq;

import com.gmoon.batchinsert.global.prop.JooqProperties;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.stereotype.Component;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.max;

/**
 * @link {https://www.jooq.org/doc/latest/manual/code-generation/codegen-programmatic/}
 */
@Component
@RequiredArgsConstructor
public class JooqGenerateSourceRunner implements ApplicationRunner {

	private final DSLContext dsl;

	private final DataSourceProperties dataSourceProperties;
	private final JooqProperties jooqProperties;
	private final FlywayProperties flywayProperties;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		Configuration configuration = new Configuration()
			 .withJdbc(new Jdbc()
				  .withDriver(dataSourceProperties.getDriverClassName())
				  .withUrl(dataSourceProperties.getUrl())
				  .withUser(dataSourceProperties.getUsername())
				  .withPassword(dataSourceProperties.getPassword()))
			 .withGenerator(new Generator()
				  .withDatabase(new Database()
					   .withSchemaVersionProvider(new FlywaySchemaVersionProvider(dsl, flywayProperties).version())
					   .withName(jooqProperties.getDriverClassName()) // org.jooq.meta.*.*Database
					   .withIncludes(".*")
					   .withExcludes("")
					   .withInputSchema(jooqProperties.getSchema())
				  )
				  .withTarget(
					   new Target()
							.withDirectory(jooqProperties.getTargetDir())
							.withPackageName("com.gmoon.batchinsert.global.jmodel")
				  )
			 );
		GenerationTool.generate(configuration);
	}

	static class FlywaySchemaVersionProvider {

		private final DSLContext dsl;
		private final String historyTable;

		FlywaySchemaVersionProvider(DSLContext dsl, FlywayProperties flywayProperties) {
			this.dsl = dsl;
			this.historyTable = flywayProperties.getTable();
		}

		public String version() {
			return dsl
				 .select(max(field("version")).as("max_version"))
				 .from(historyTable)
				 .fetchSingle("max_version", String.class);
		}
	}
}
