package com.gmoon.querydslsql.generator;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import com.querydsl.sql.codegen.MetaDataExporter;
import jakarta.persistence.Entity;
import lombok.ToString;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.cfg.Configuration;
import org.hibernate.relational.internal.SchemaManagerImpl;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.internal.HibernateSchemaManagementTool;
import org.hibernate.tool.schema.spi.SchemaCreator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * <a href="https://stackoverflow.com/questions/74095282/hibernate-6-schemaexport-class-not-found">hibernate6 SchemaExport class not found</a>
 * https://hibernate.org/tools/
 * https://github.com/hibernate/hibernate-tools
 * https://central.sonatype.com/artifact/org.hibernate/hibernate-tools-maven-plugin
 * </pre>
 *
 * @see MetadataSources
 * @see StandardServiceRegistry
 * @see HibernateSchemaManagementTool
 * @see SchemaCreator
 * @see org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl#generateSchema
 * @see jakarta.persistence.Persistence#generateSchema
 * @see SchemaManagerImpl
 * @see SchemaExport
 */
public class QueryDslSQLMetaModelGenerator {
	private static final String TOOL = "QDSL-SQL";

	private final MetaDataExporterOption option;

	private QueryDslSQLMetaModelGenerator(String[] args) {
		this.option = new MetaDataExporterOption(args);
	}

	public static void main(String[] args) {
		QueryDslSQLMetaModelGenerator generator = new QueryDslSQLMetaModelGenerator(args);
		MetaDataExporterOption option = generator.option;
		Log log = new Log();

		log.banner(TOOL + " GENERATOR START");
		log.info("Input args : " + Arrays.toString(args));
		log.info("Options    : " + option);
		String jdbcUrl = option.jdbcUrl;
		String jdbcUser = option.jdbcUser;
		String jdbcPassword = option.jdbcPassword;
		log.step("DB Connection");
		log.info("Connecting to database: " + jdbcUrl + " (user: " + jdbcUser + ")");
		try (Connection conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword)) {
			log.step("Hibernate Session Opening");
			try (SessionFactory sf = generator.createSessionFactory()) {
				long start = System.currentTimeMillis();
				log.step("MetaModel Generation");
				generator.generateMetaModel(conn);  // log 객체 전달
				long end = System.currentTimeMillis();
				log.info("MetaModel Generation Done (" + (end - start) + "ms)");
			}
		} catch (Exception ex) {
			log.error("Code generation failed: " + ex.getMessage());
			ex.printStackTrace(System.err);
			System.exit(1);
		} finally {
			AbandonedConnectionCleanupThread.checkedShutdown();
		}
		log.info("QueryDSL-SQL meta-model classes generation completed successfully.");
		log.banner(TOOL + "GENERATOR END");
	}

	private void generateMetaModel(Connection conn) throws SQLException {
		MetaDataExporter exporter = new MetaDataExporter();
		exporter.setSchemaPattern(option.schema);
		exporter.setCatalogPattern(option.schema);
		exporter.setNamePrefix("S");
		exporter.setPackageName("com.gmoon.querydslsql.sdomains");
		exporter.setTargetFolder(new File(option.targetFolder));
		exporter.export(conn.getMetaData());
	}

	/**
	 * @see org.hibernate.cfg.AvailableSettings
	 */
	private SessionFactory createSessionFactory() {
		Configuration config = new Configuration();
		String schema = option.schema;
		String jdbcUrl = option.jdbcUrl;
		String jdbcFqnUrl = String.format("%s/%s?createDatabaseIfNotExist=true", jdbcUrl, schema);
		config.setProperty("hibernate.connection.driver_class", option.jdbcDriver);
		config.setProperty("hibernate.connection.url", jdbcFqnUrl);
		config.setProperty("hibernate.connection.username", option.jdbcUser);
		config.setProperty("hibernate.connection.password", option.jdbcPassword);
		config.setProperty("hibernate.hbm2ddl.auto", "create-drop");
		config.setProperty("hibernate.dialect", option.dialect);
		config.setProperty("hibernate.show_sql", "true");

		// Entity 클래스 명시적으로 등록
		// config.addAnnotatedClass(com.gmoon.querydslsql.coupons.PointTransaction.class);
		String basePackage = "com.gmoon.querydslsql";
		registerEntityClasses(config, basePackage);
		return config.buildSessionFactory();
	}

	private void registerEntityClasses(Configuration config, String basePackage) {
		ClassPathScanningCandidateComponentProvider scanner =
			 new ClassPathScanningCandidateComponentProvider(false);

		// @Entity 붙은 클래스만 스캔 대상
		scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));

		for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
			try {
				Class<?> clazz = Class.forName(bd.getBeanClassName());
				config.addAnnotatedClass(clazz);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Entity class not found: " + bd.getBeanClassName(), e);
			}
		}
	}

	@ToString
	static class MetaDataExporterOption {
		public final String jdbcDriver;
		public final String jdbcUrl;
		public final String jdbcUser;
		public final String jdbcPassword;
		public final String schema;
		public final String dialect;
		public final String targetFolder;

		public MetaDataExporterOption(String[] args) {
			Map<String, String> map = new HashMap<>();
			for (String arg : args) {
				if (arg.startsWith("--")) {
					String[] split = arg.substring(2).split("=", 2);
					if (split.length == 2) {
						map.put(split[0], split[1]);
					}
				}
			}
			this.jdbcDriver = map.get("jdbcDriver");
			this.jdbcUrl = map.get("jdbcUrl");
			this.jdbcUser = map.get("jdbcUser");
			this.jdbcPassword = map.get("jdbcPassword");
			this.schema = map.get("dbSchema");
			this.dialect = map.get("hibernateDialect");
			this.targetFolder = map.get("targetFolder");
		}
	}

	static class Log {
		private final String RESET = "\u001B[0m";
		private final String RED = "\u001B[31m";
		private final String GREEN = "\u001B[32m";
		private final String YELLOW = "\u001B[33m";
		private final String BLUE = "\u001B[34m";
		private final String CYAN = "\u001B[36m";
		private final String BOLD = "\u001B[1m";

		public void info(String msg) {
			System.out.println(GREEN + "[" + TOOL + "] " + msg + RESET);
		}

		public void warn(String msg) {
			System.out.println(YELLOW + "[" + TOOL + "][WARN] " + msg + RESET);
		}

		public void step(String msg) {
			System.out.println(BLUE + "[" + TOOL + "] " + msg + RESET);
		}

		public void error(String msg) {
			System.err.println(RED + "[" + TOOL + "][ERROR] " + msg + RESET);
		}

		public void banner(String msg) {
			System.out.println(BOLD + GREEN + "\n================== " + msg + " ==================" + RESET);
		}
	}
}
