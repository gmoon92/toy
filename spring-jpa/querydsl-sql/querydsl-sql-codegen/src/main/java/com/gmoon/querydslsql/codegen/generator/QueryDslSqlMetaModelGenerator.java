package com.gmoon.querydslsql.codegen.generator;

import com.gmoon.querydslsql.codegen.generator.logging.ConsoleLog;
import com.querydsl.sql.codegen.MetaDataExporter;
import jakarta.persistence.Entity;
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
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Enumeration;

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
			log.step("DB Connection closing.");
			generator.closeDbConnection();
			log.step("DB Connection closed.");
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

	/**
	 * <pre>
	 * MySQL Connector/J의 `AbandonedConnectionCleanupThread`는
	 * 드라이버의 리소스를 관리하기 위한 백그라운드 스레드다.
	 * - 과거(5.x~8.x)엔 명시적으로 checkedShutdown() 호출이 필요했고,
	 *   그래도 JVM 종료 시 스레드가 계속 살아있어 다음과 같은 WARNING이 발생할 수 있다.
	 *
	 * [WARNING] thread Thread[#53,mysql-cj-abandoned-connection-cleanup,5,main] was interrupted but is still alive after waiting at least 15000msecs
	 * [WARNING] thread ... will linger despite being asked to die via interruption
	 * [WARNING] NOTE: 1 thread(s) did not finish despite being asked to via interruption. This is not a problem with exec:java, it is a problem with the running code. Although not serious, it should be remedied.
	 *
	 * - 최신 9.x에서는 해당 cleanup thread가 사라지거나 동작 방식이 변경되어 있어 일반적으로 이런 경고가 더는 발생하지 않아야 정상이다.
	 * - 그러나 프로젝트에 복수 또는 구버전(8.x) 드라이버가 classpath 상에 섞여 있거나,
	 * exec-maven-plugin 등 별도 JVM환경(classloader isolation)에서 실행하는 경우 여전히 나타날 수 있다.
	 *
	 * 해결 방법:
	 *    1. 가능한 모든 Connection, DataSource를 명시적으로 close하고,
	 *    2. DriverManager에서 MySQL 드라이버를 deregister(언로드) 하며,
	 *    3. (8.x 이하 구버전일 경우에만) AbandonedConnectionCleanupThread.checkedShutdown() 호출
	 *
	 * 아래 closeDbConnection() 메서드가 이를 수행합니다.
	 * </pre>
	 */
	private void closeDbConnection() {
		try {
			Enumeration<Driver> drivers = DriverManager.getDrivers();
			while (drivers.hasMoreElements()) {
				Driver driver = drivers.nextElement();
				if (driver.getClass().getName().contains("mysql")) {
					try {
						DriverManager.deregisterDriver(driver);
					} catch (Exception e) {
						log.error("Failed to deregister JDBC driver (" + driver.getClass().getName() + "): " + e.getMessage());
						throw e;
					}
				}
			}
		} catch (Exception e) {
			log.error("Failed to enumerate JDBC drivers: " + e.getMessage());
			e.printStackTrace(System.err);
		}

		try {
			// 8.x 구버전 호환: AbandonedConnectionCleanupThread shutdown 호출
			// 9.x 이상이면 이 클래스가 없어도 정상.
			Class<?> cleanupThread = Class.forName("com.mysql.cj.jdbc.AbandonedConnectionCleanupThread");
			cleanupThread.getMethod("checkedShutdown").invoke(null);
		} catch (ClassNotFoundException ignore) {
		} catch (Exception e) {
			log.error("Failed to shutdown AbandonedConnectionCleanupThread: " + e.getMessage());
			e.printStackTrace(System.err);
		}
	}
}
