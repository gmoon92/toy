package com.gmoon.querydslsql.querydslsqlmaven;

import java.io.File;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.MappingSettings;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.gmoon.querydslsql.querydslsqlmaven.logging.ConsoleLog;
import com.google.common.annotations.VisibleForTesting;
import com.querydsl.sql.codegen.MetaDataExporter;

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

	@Parameter
	private List<String> compilerOptions = new ArrayList<>();

	private JavaCompilerHelper javaCompilerHelper;

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
		setupTargetFolder(targetFolder);
	}

	private void setupTargetFolder(String targetFolder) throws MojoExecutionException {
		log.info("target: " + targetFolder + " targetDirectory: " + generatedSourcesDirectory);
		File targetDirectory = new File(generatedSourcesDirectory, targetFolder);
		if (!targetDirectory.exists()) {
			boolean created = targetDirectory.mkdirs();
			if (!created) {
				throw new MojoExecutionException("Failed to create target directory: " + targetDirectory);
			}
		}
		this.targetFolder = targetDirectory;
		javaCompilerHelper = new JavaCompilerHelper(mavenProject, compilerOptions, testClasspath);
	}

	@Override
	public void execute() throws MojoExecutionException {
		log.banner("QUERYDSL META MODEL GENERATION START");
		setupTargetFolder("qeurydslsql");
		log.info("Config: " + config);
		ensureDirectoryExists(targetFolder, "Source");
		ensureDirectoryExists(classesDirectory, "Classes");

		generateMetaModel();
		scanMetaModelSources();
		compileMetaModelSources();

		log.info("QueryDSL SQL meta-model class generation and compilation finished successfully.");
		log.banner("QUERYDSL META MODEL GENERATION END");
	}

	private void generateMetaModel() throws MojoExecutionException {
		log.step("DB Connection");
		String jdbcUrl = config.getJdbcUrl();
		String jdbcUser = config.getJdbcUser();
		Metadata metadata = getMetadata();
		log.info("Connecting: " + jdbcUrl + " (user: " + jdbcUser + ")");
		try (Connection conn = DriverManager.getConnection(jdbcUrl, jdbcUser, config.getJdbcPassword())) {
			createSchema(metadata);
			generateMetaModel(conn);
		} catch (Exception ex) {
			log.error("Code generation failed: " + ex.getMessage());
			ex.printStackTrace(System.err);
		} finally {
			log.step("DB Connection closing.");
			dropSchema(metadata);
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
		List<File> found = javaCompilerHelper.findJavaFiles(targetFolder);
		for (File candidateFile : found) {
			if (candidateFile.isFile()) {
				metaModelSources.add(candidateFile);
				log.step("Found metamodel source: " + candidateFile.getAbsolutePath());
			}
		}
		log.step("Total metamodel sources found: " + metaModelSources.size());
	}

	private void compileMetaModelSources() throws MojoExecutionException {
		log.info("Metamodel compiling " + metaModelSources.size() + " metamodel source files...");
		javaCompilerHelper.compileJavaFiles(metaModelSources, classesDirectory);

		log.info("Metamodel source compilation finished.");
	}

	/**
	 * @see <a href="https://www.theserverside.com/blog/Coffee-Talk-Java-News-Stories-and-Opinions/A-version-5-Hibernate-SchemaExport-example-with-the-ServiceRegistry-and-Metadata">Configure Hibernate metadata</a>
	 * @param metadata
	 */
	private void createSchema(Metadata metadata) {
		log.step("Create Schema.");
		SchemaExport schemaExport = new SchemaExport();
		schemaExport.setHaltOnError(true);
		schemaExport.setOutputFile(classesDirectory + "/hibernate-schema.sql");
		schemaExport.create(
			 EnumSet.of(TargetType.SCRIPT, TargetType.STDOUT, TargetType.DATABASE),
			 metadata
		);
	}

	private void dropSchema(Metadata metadata) {
		log.step("Drop Schema");
		SchemaExport schemaExport = new SchemaExport();
		schemaExport.setHaltOnError(true);
		schemaExport.drop(
			 EnumSet.of(TargetType.STDOUT, TargetType.DATABASE),
			 metadata
		);
	}

	private Metadata getMetadata() throws MojoExecutionException {
		Properties hibernateProperties = getHibernateProperties();
		StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
			 .applySettings(hibernateProperties)
			 .build();

		MetadataSources metadataSources = new MetadataSources(serviceRegistry);
		addEntityClasses(metadataSources);
		return metadataSources
			 .getMetadataBuilder()
			 .build();
	}

	private void addEntityClasses(MetadataSources sources) throws MojoExecutionException {
		File srcMainJava = new File(mavenProject.getBasedir(), "src/main/java");
		File tempClassDir = new File(mavenProject.getBuild().getDirectory(), "tmp/entity-classes");
		List<File> javaFiles = javaCompilerHelper.findJavaFiles(srcMainJava);
		List<String> entityFqns = new ArrayList<>();
		List<File> entityJavaFiles = new ArrayList<>();
		log.info("Scanning for @Entity classes in " + srcMainJava.getAbsolutePath());
		for (File file : javaFiles) {
			try {
				CompilationUnit cu = StaticJavaParser.parse(file);
				cu.findAll(ClassOrInterfaceDeclaration.class)
					 .forEach(clazz -> {
						 if (clazz.getAnnotationByName("Entity").isPresent()) {
							 String packageName = cu.getPackageDeclaration()
								  .map(pd -> pd.getName().toString())
								  .orElse("");
							 String className = clazz.getNameAsString();
							 String fqn = packageName.isEmpty() ? className : packageName + "." + className;
							 log.info("Found entity: " + fqn);
							 entityFqns.add(fqn);
							 entityJavaFiles.add(file);
						 }
					 });
			} catch (Exception e) {
				log.warn("Parse error on file: " + file + " " + e.getMessage());
			}
		}

		javaCompilerHelper.compileJavaFiles(entityJavaFiles, tempClassDir);
		javaCompilerHelper.registerClassLoader(tempClassDir);
		 for (String fqn : entityFqns) {
			 sources.addAnnotatedClassName(fqn);
		 }
	}

	private Properties getHibernateProperties() {
		String schema = config.getSchema();
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
		// JPA/Hibernate 6.x 이상에서는 set*NamingStrategy 사용.
		//        Configuration configuration = new Configuration();
		//        configuration.setImplicitNamingStrategy(new SpringImplicitNamingStrategy());
		settings.setProperty(MappingSettings.IMPLICIT_NAMING_STRATEGY,
			 "org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl");
		settings.setProperty(MappingSettings.PHYSICAL_NAMING_STRATEGY,
			 CamelCaseToUnderscoresNamingStrategy.class.getName());
		return settings;
	}

	private void generateMetaModel(Connection conn) throws SQLException {
		long start = System.currentTimeMillis();
		log.step("MetaModel Generation");
		String targetPackage = config.getBasePackage() + ".querydslsql";
		String queryDslSqlSchema = config.getSchema();
		log.info("target schema : " + queryDslSqlSchema);
		log.info("target package: " + targetPackage);
		log.info("target folder : " + targetFolder.getAbsolutePath());
		MetaDataExporter exporter = new MetaDataExporter();
		exporter.setSchemaPattern(queryDslSqlSchema);
		exporter.setCatalogPattern(queryDslSqlSchema);
		exporter.setPackageName(targetPackage);

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
