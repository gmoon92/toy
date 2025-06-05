package com.gmoon.querydslsql.querydslsqlmaven;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.MappingSettings;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;

import com.gmoon.querydslsql.querydslsqlmaven.logging.ConsoleLog;
import com.google.common.annotations.VisibleForTesting;
import com.querydsl.sql.codegen.MetaDataExporter;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import jakarta.persistence.Entity;

/**
 * Maven Mojo for QueryDSL SQL MetaModel code generation and on-the-fly compilation.
 * <p>
 * - 엔티티 객체에 기반한 메타모델(QueryDSL Q-클래스) 자동 생성
 * - 생성된 메타모델 소스 코드의 컴파일 자동화
 * </p>
 *
 * @author gmoon
 * @see <a href="https://maven.apache.org/guides/plugin/guide-java-plugin-development.html">Apache Maven Plugin Guide</a>
 */
@Mojo(
	 name = "process",
	 defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
	 requiresDependencyResolution = ResolutionScope.COMPILE
)
public class QuerydslSqlMetamodelGenerateMojo extends AbstractMojo {
	private static final ConsoleLog log = new ConsoleLog("QDSL-SQL");

	private final List<File> metaModelSources = new ArrayList<>();

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject mavenProject;

	@Parameter(defaultValue = "${project.build.directory}/generated-sources/java/", readonly = true, required = true)
	private File generatedSourcesDirectory;

	@Parameter(defaultValue = "${project.build.directory}/classes", readonly = true)
	private File classesDirectory;

	@Parameter(required = true)
	private Config config;

	private File targetFolder;

	@Parameter(defaultValue = "false")
	private boolean testClasspath;

	public QuerydslSqlMetamodelGenerateMojo() {
	}

	public QuerydslSqlMetamodelGenerateMojo(
		 MavenProject mavenProject,
		 File generatedSourcesDirectory,
		 String targetFolder,
		 File classesDirectory
	) throws MojoExecutionException {
		this.mavenProject = mavenProject;
		this.generatedSourcesDirectory = generatedSourcesDirectory;
		this.classesDirectory = classesDirectory;
		setup(targetFolder);
	}

	private void setup(String targetFolder) throws MojoExecutionException {
		this.targetFolder = getFileDir(generatedSourcesDirectory, targetFolder);
	}

	private File getFileDir(File target, String targetFolder) throws MojoExecutionException {
		log.info("target: " + targetFolder + " targetDirectory: " + target);
		File targetDirectory = new File(target, targetFolder);
		if (!targetDirectory.exists()) {
			boolean created = targetDirectory.mkdirs();
			if (!created) {
				throw new MojoExecutionException("Failed to create target directory: " + targetDirectory);
			}
		}
		return targetDirectory;
	}

	@Override
	public void execute() throws MojoExecutionException {
		log.banner("QUERYDSL META MODEL GENERATION START");
		setup("qeurydslsql");
		log.info("Config: " + config);
		ensureDirectoryExists(targetFolder, "Source");
		ensureDirectoryExists(classesDirectory, "Classes");

		generateMetaModel();
		scanMetaModelSources();
		compileMetaModelSources();

		log.info("QueryDSL SQL meta-model class generation and compilation finished successfully.");
		log.banner("QUERYDSL META MODEL GENERATION END");
	}

	private void generateMetaModel() {
		log.step("DB Connection");
		log.info("Connecting: " + config.getJdbcUrl() + " (user: " + config.getJdbcUser() + ")");
		try (Connection conn = DriverManager.getConnection(config.getJdbcUrl(), config.getJdbcUser(),
			 config.getJdbcPassword())) {
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

	private void ensureDirectoryExists(File dir, String kind) throws MojoExecutionException {
		if (dir == null) {
			throw new MojoExecutionException(kind + " directory is null.");
		}
		if (!dir.exists() && !dir.mkdirs()) {
			throw new MojoExecutionException("Could not create " + kind.toLowerCase() + " directory: " + dir);
		}
	}

	private void scanMetaModelSources() {
		log.step("Scanning generated metamodel sources in: " + targetFolder.getAbsolutePath());
		FileSet fileSet = new FileSet();
		fileSet.setDirectory(targetFolder.getAbsolutePath());
		fileSet.addInclude("**/*.java");

		FileSetManager fileSetManager = new FileSetManager();
		String[] files = fileSetManager.getIncludedFiles(fileSet);
		for (String fileName : files) {
			File candidateFile = new File(targetFolder, fileName);
			if (candidateFile.isFile()) {
				metaModelSources.add(candidateFile);
				log.step("Found metamodel source: " + candidateFile.getAbsolutePath());
			}
		}
		log.step("Total metamodel sources found: " + metaModelSources.size());
	}

	private void compileMetaModelSources() throws MojoExecutionException {
		if (metaModelSources.isEmpty()) {
			log.info("No metamodel sources found. Skipping compile step.");
			return;
		}
		log.info("Compiling " + metaModelSources.size() + " metamodel source files...");
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			throw new MojoExecutionException("JDK required for source compilation (no javac found).");
		}

		List<String> classpathElements = getClasspathElements();
		String classpath = classpathElements.isEmpty() ? "" : String.join(File.pathSeparator, classpathElements);

		for (File sourceFile : metaModelSources) {
			log.info("Compiling: " + sourceFile.getAbsolutePath());
			try (ByteArrayOutputStream errStream = new ByteArrayOutputStream()) {
				int result = compiler.run(
					 null,
					 null,
					 errStream,
					 "-classpath", classpath,
					 "-d", classesDirectory.getAbsolutePath(),
					 sourceFile.getAbsolutePath()
				);
				if (result != 0) {
					String errorMsg = "Compilation failed for " + sourceFile.getName() + ":\n" + errStream.toString(
						 StandardCharsets.UTF_8);
					log.error(errorMsg);
					throw new MojoExecutionException(errorMsg);
				}
			} catch (IOException e) {
				throw new MojoExecutionException("Error compiling: " + sourceFile.getAbsolutePath(), e);
			}
		}
		log.info("Metamodel source compilation finished.");
	}

	private List<String> getClasspathElements() throws MojoExecutionException {
		try {
			if (testClasspath) {
				return mavenProject.getTestClasspathElements();
			}
			return mavenProject.getCompileClasspathElements();
		} catch (Exception e) {
			throw new MojoExecutionException("Could not get classpath elements.", e);
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
		schemaExport.setOutputFile(classesDirectory + "/hibernate-schema.sql");
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
		String pkg = config.getBasePackage();
		String classesPath = classesDirectory.getAbsolutePath();
		log.info(String.format("Scanning for @Entity classes in package=%s under=%s", pkg, classesPath));
		try (ScanResult scanResult = new ClassGraph()
			 .enableAnnotationInfo()
			 .overrideClasspath(classesPath)
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
			String classPath = classesDirectory.getAbsolutePath();
			URL[] urls = new URL[] {new File(classPath).toURI().toURL()};
			URLClassLoader urlClassLoader = new URLClassLoader(urls, getClass().getClassLoader());
			Thread.currentThread().setContextClassLoader(urlClassLoader); // 혹은 아래와 같이 Reflections/CG에 넘김
			return urlClassLoader;
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	private Properties getHibernateProperties() {
		String schema = getQueryDslSqlSchema();
		String jdbcUrl = config.getJdbcUrl();
		String jdbcFqnUrl = String.format("%s/%s?createDatabaseIfNotExist=true", jdbcUrl, schema);
		Properties settings = new Properties();
		settings.setProperty("hibernate.connection.driver_class", config.getJdbcDriver());
		settings.setProperty("hibernate.connection.url", jdbcFqnUrl);
		settings.setProperty("hibernate.connection.username", config.getJdbcUser());
		settings.setProperty("hibernate.connection.password", config.getJdbcPassword());
		settings.setProperty("hibernate.dialect", config.getDialect());
		settings.setProperty("hibernate.show_sql", "true");
		settings.setProperty("hibernate.format_sql", "true");
		settings.setProperty("hibernate.hbm2ddl.auto", "create-drop");
		// JPA/Hibernate 6.x 이상에서는 set*NamingStrategy 사용.
		//        Configuration configuration = new Configuration();
		//        configuration.setImplicitNamingStrategy(new SpringImplicitNamingStrategy());
		settings.setProperty(MappingSettings.IMPLICIT_NAMING_STRATEGY,
			 "org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl");
		settings.setProperty(MappingSettings.PHYSICAL_NAMING_STRATEGY,
			 CamelCaseToUnderscoresNamingStrategy.class.getName());
		return settings;
	}

	private String getQueryDslSqlSchema() {
		return config.getSchema() + "_querydslsql";
	}

	private void generateMetaModel(Connection conn) throws SQLException {
		long start = System.currentTimeMillis();
		log.step("MetaModel Generation");
		MetaDataExporter exporter = new MetaDataExporter();
		exporter.setSchemaPattern(getQueryDslSqlSchema());
		exporter.setCatalogPattern(getQueryDslSqlSchema());
		exporter.setPackageName(config.getTargetPackage());

		exporter.setTargetFolder(targetFolder);
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

	@VisibleForTesting
	List<File> getMetaModelSources() {
		return metaModelSources;
	}

	@VisibleForTesting
	File getTargetFolder() {
		return targetFolder;
	}
}
