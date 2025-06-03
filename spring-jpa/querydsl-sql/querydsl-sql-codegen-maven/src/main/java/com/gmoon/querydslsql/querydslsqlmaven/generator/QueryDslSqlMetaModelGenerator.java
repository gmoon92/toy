package com.gmoon.querydslsql.querydslsqlmaven.generator;

import com.gmoon.querydslsql.querydslsqlmaven.logging.ConsoleLog;
import com.querydsl.sql.codegen.MetaDataExporter;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import jakarta.persistence.Entity;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.MappingSettings;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.Properties;

/**
 * QueryDSL SQL MetaModel & Schema Generator.
 * <p>
 * - 엔티티 클래스 스캔(@Entity)
 * - Hibernate로 스키마 DDL 생성 및 export (DB, 파일, 콘솔 지원)
 * - QueryDSL MetaModel 클래스 자동 생성
 * </p>
 *
 * @see <a href="https://www.theserverside.com/blog/Coffee-Talk-Java-News-Stories-and-Opinions/A-version-5-Hibernate-SchemaExport-example-with-the-ServiceRegistry-and-Metadata">Hibernate SchemaExport 예제</a>
 */
public class QueryDslSqlMetaModelGenerator {
    private final ConsoleLog log;
    private final Config config;

    public QueryDslSqlMetaModelGenerator(ConsoleLog log, Config config) {
        this.log = log;
        this.config = config;
    }

    public void generate() {
        log.step("DB Connection");
        log.info("Connecting: " + config.jdbcUrl + " (user: " + config.jdbcUser + ")");
        try (Connection conn = DriverManager.getConnection(config.jdbcUrl, config.jdbcUser, config.jdbcPassword)) {
            createSchema();
            generateMetaModel(conn);
        } catch (Exception ex) {
            log.error("Code generation failed: " + ex.getMessage());
            ex.printStackTrace(System.err);
            System.exit(1);
        } finally {
            log.step("DB Connection closing.");
            closeDbConnection();
            log.step("DB Connection closed.");
        }
    }

    /**
     * @see <a href="https://www.theserverside.com/blog/Coffee-Talk-Java-News-Stories-and-Opinions/A-version-5-Hibernate-SchemaExport-example-with-the-ServiceRegistry-and-Metadata">Configure Hibernate metadata</a>
     */
    private void createSchema() {
        log.step("Schema Export (Hibernate)");
        Properties hibernateProperties = getHibernateProperties();
        StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(hibernateProperties)
                .build();

        Metadata metadata = getMetadata(serviceRegistry);

        SchemaExport schemaExport = new SchemaExport();
        schemaExport.setHaltOnError(true);
        schemaExport.setOutputFile(config.outputDirectory + "/hibernate-schema.sql");
        schemaExport.create(
                EnumSet.of(TargetType.SCRIPT, TargetType.STDOUT, TargetType.DATABASE),
                metadata
        );
    }

    private Metadata getMetadata(StandardServiceRegistry serviceRegistry) {
        MetadataSources metadataSources = new MetadataSources(serviceRegistry);
        addEntityClasses(metadataSources);
        return metadataSources
                .getMetadataBuilder()
                .build();
    }

    private void addEntityClasses(MetadataSources sources) {
        String pkg = config.entityBasePackage;
        String classesDir = config.outputDirectory;
        log.info(String.format("Scanning for @Entity classes in package=%s under=%s", pkg, classesDir));
        try (ScanResult scanResult = new ClassGraph()
                .enableAnnotationInfo()
                .overrideClasspath(classesDir)
                .acceptPackages(pkg)
                .addClassLoader(buildProjectClassLoader())
                .scan()) {
            for (ClassInfo ci : scanResult.getClassesWithAnnotation(Entity.class.getName())) {
                Class<?> clazz = ci.loadClass();
                log.info(String.format("Found entity: %s", clazz.getName()));
                sources.addAnnotatedClassName(clazz.getName());
            }
        }
    }

    private URLClassLoader buildProjectClassLoader() {
        try {
            String classPath = config.outputDirectory;
            URL[] urls = new URL[]{new File(classPath).toURI().toURL()};
            URLClassLoader urlClassLoader = new URLClassLoader(urls, getClass().getClassLoader());
            Thread.currentThread().setContextClassLoader(urlClassLoader); // 혹은 아래와 같이 Reflections/CG에 넘김
            return urlClassLoader;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private Properties getHibernateProperties() {
        String schema = config.schema;
        String jdbcUrl = config.jdbcUrl;
        String jdbcFqnUrl = String.format("%s/%s?createDatabaseIfNotExist=true", jdbcUrl, schema);
        Properties settings = new Properties();
        settings.setProperty("hibernate.connection.driver_class", config.jdbcDriver);
        settings.setProperty("hibernate.connection.url", jdbcFqnUrl);
        settings.setProperty("hibernate.connection.username", config.jdbcUser);
        settings.setProperty("hibernate.connection.password", config.jdbcPassword);
        settings.setProperty("hibernate.dialect", config.dialect);
        settings.setProperty("hibernate.show_sql", "true");
        settings.setProperty("hibernate.format_sql", "true");
        settings.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        // JPA/Hibernate 6.x 이상에서는 set*NamingStrategy 사용.
//        Configuration configuration = new Configuration();
//        configuration.setImplicitNamingStrategy(new SpringImplicitNamingStrategy());
        settings.setProperty(MappingSettings.IMPLICIT_NAMING_STRATEGY, "org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl");
        settings.setProperty(MappingSettings.PHYSICAL_NAMING_STRATEGY, CamelCaseToUnderscoresNamingStrategy.class.getName());
        return settings;
    }

    private void generateMetaModel(Connection conn) throws SQLException {
        long start = System.currentTimeMillis();
        log.step("MetaModel Generation");
        MetaDataExporter exporter = new MetaDataExporter();
        exporter.setSchemaPattern(config.schema);
        exporter.setCatalogPattern(config.schema);
        exporter.setPackageName(config.targetPackage);
        exporter.setTargetFolder(new File(config.targetFolder));
        exporter.export(conn.getMetaData());
        long end = System.currentTimeMillis();
        log.step("MetaModel Generation Done (" + (end - start) + "ms)");
    }

    private void closeDbConnection() {
        try {
            Enumeration<Driver> drivers = DriverManager.getDrivers();
            while (drivers.hasMoreElements()) {
                Driver driver = drivers.nextElement();
                if (driver.getClass().getName().contains("mysql")) {
                    try {
                        DriverManager.deregisterDriver(driver);
                    } catch (Exception e) {
                        log.error("Failed to deregister JDBC driver (" + driver.getClass().getName() + "): "
                                + e.getMessage());
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
