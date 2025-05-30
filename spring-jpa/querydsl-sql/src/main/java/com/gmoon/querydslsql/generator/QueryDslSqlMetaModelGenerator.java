package com.gmoon.querydslsql.generator;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import com.querydsl.sql.codegen.MetaDataExporter;

import jakarta.persistence.Entity;
import lombok.ToString;

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
public class QueryDslSqlMetaModelGenerator {
	private static final ConsoleLog log = new ConsoleLog("QDSL-SQL");

	private final Config config;

	private QueryDslSqlMetaModelGenerator(String[] args) {
		this.config = new Config(args);
	}

	public static void main(String[] args) {
		QueryDslSqlMetaModelGenerator generator = new QueryDslSqlMetaModelGenerator(args);
		Config option = generator.config;

		log.banner("GENERATOR START");
		log.info("Input args : " + Arrays.toString(args));
		log.info("Config     : " + option);
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
				generator.generateMetaModel(conn);
				long end = System.currentTimeMillis();
				log.step("MetaModel Generation Done (" + (end - start) + "ms)");
			}
		} catch (Exception ex) {
			log.error("Code generation failed: " + ex.getMessage());
			ex.printStackTrace(System.err);
			System.exit(1);
		} finally {
			AbandonedConnectionCleanupThread.checkedShutdown();
		}
		log.info("QueryDSL-SQL meta-model classes generation completed successfully.");
		log.banner("GENERATOR END");
	}

	/**
	 * @see org.hibernate.cfg.AvailableSettings
	 */
	private SessionFactory createSessionFactory() {
		Configuration configuration = new Configuration();
		String schema = config.schema;
		String jdbcUrl = config.jdbcUrl;
		String jdbcFqnUrl = String.format("%s/%s?createDatabaseIfNotExist=true", jdbcUrl, schema);
		configuration.setProperty("hibernate.connection.driver_class", config.jdbcDriver);
		configuration.setProperty("hibernate.connection.url", jdbcFqnUrl);
		configuration.setProperty("hibernate.connection.username", config.jdbcUser);
		configuration.setProperty("hibernate.connection.password", config.jdbcPassword);
		configuration.setProperty("hibernate.hbm2ddl.auto", "create-drop");
		configuration.setProperty("hibernate.dialect", config.dialect);
		configuration.setProperty("hibernate.show_sql", "true");

		String basePackage = config.entityBasePackage;
		registerEntityClasses(configuration, basePackage);
		return configuration.buildSessionFactory();
	}

	private void registerEntityClasses(Configuration configuration, String basePackage) {
		ClassPathScanningCandidateComponentProvider scanner =
			 new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));

		for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
			try {
				Class<?> clazz = Class.forName(bd.getBeanClassName());
				configuration.addAnnotatedClass(clazz);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Entity class not found: " + bd.getBeanClassName(), e);
			}
		}
	}

	private void generateMetaModel(Connection conn) throws SQLException {
		MetaDataExporter exporter = new MetaDataExporter();
		exporter.setSchemaPattern(config.schema);
		exporter.setCatalogPattern(config.schema);
		exporter.setNamePrefix(config.namePrefix);
		exporter.setPackageName(config.targetPackage);
		exporter.setTargetFolder(new File(config.targetFolder));
		exporter.export(conn.getMetaData());
	}

	@ToString
	static class Config {
		public final String jdbcDriver;
		public final String jdbcUrl;
		public final String jdbcUser;
		public final String jdbcPassword;
		public final String schema;
		public final String dialect;
		public final String entityBasePackage;
		public final String targetFolder;
		public final String targetPackage;
		public final String namePrefix;

		public Config(String[] args) {
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
			this.schema = map.get("schema");
			this.dialect = map.get("dialect");
			this.namePrefix = map.get("namePrefix");
			this.entityBasePackage = map.get("entityBasePackage");
			this.targetPackage = map.get("targetPackage");
			this.targetFolder = map.get("targetFolder");
		}
	}

	static class ConsoleLog {
		private final String label;

		public ConsoleLog(String label) {
			this.label = label;
		}

		public void info(String msg) {
			String message = formatMsg(Color.GREEN, "", msg);
			System.out.println(message);
		}

		public void warn(String msg) {
			String message = formatMsg(Color.YELLOW, "[WARN]", msg);
			System.out.println(message);
		}

		public void step(String msg) {
			String message = formatMsg(Color.BLUE, "", msg);
			System.out.println(message);
		}

		public void error(String msg) {
			String message = formatMsg(Color.RED, "[ERROR]", msg);
			System.err.println(message);
		}

		private String formatMsg(Color color, String prefix, String msg) {
			String format = String.format("[%s]%s %s", label, prefix, msg);
			return color.apply(format);
		}

		public void banner(String msg) {
			String bannerMsg = String.format("================== %s %s ==================", label, msg);
			System.out.println("\n" + Color.bold(Color.GREEN, bannerMsg) + "\n");
		}
	}

	public enum Color {
		RESET("\u001B[0m"),
		RED("\u001B[31m"),
		GREEN("\u001B[32m"),
		YELLOW("\u001B[33m"),
		BLUE("\u001B[34m"),
		CYAN("\u001B[36m");

		private final String code;

		Color(String code) {
			this.code = code;
		}

		public String apply(String text) {
			return code + text + RESET.code;
		}

		public static String bold(Color color, String text) {
			return "\u001B[1m" + color.code + text + RESET.code;
		}
	}
}
