package com.gmoon.querydslsql.querydslsqlmaven;

import com.gmoon.querydslsql.querydslsqlmaven.generator.Config;
import com.gmoon.querydslsql.querydslsqlmaven.generator.QueryDslSqlMetaModelGenerator;
import com.gmoon.querydslsql.querydslsqlmaven.logging.ConsoleLog;
import com.google.common.annotations.VisibleForTesting;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
		name = "generate-querydsl-sql-metamodel",
		defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
	 requiresDependencyResolution = ResolutionScope.COMPILE
)
public class QuerydslSqlMetamodelGenerateMojo extends AbstractMojo {
	private static final ConsoleLog log = new ConsoleLog("QDSL-SQL");

	private final List<File> metaModelSources = new ArrayList<>();

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject mavenProject;

	@Parameter(defaultValue = "${project.build.directory}/generated-sources/java/querydslsql", required = true)
	private File sourceDirectory;

	@Parameter(defaultValue = "${project.build.directory}/classes", required = true)
	private File classesDirectory;

	@Parameter(defaultValue = "false")
	private boolean testClasspath;

	private Config config;

	@Parameter(property = "options")
	public void setOptions(String[] options) {
		config = new Config(options);
	}

	public QuerydslSqlMetamodelGenerateMojo() {
	}

	public QuerydslSqlMetamodelGenerateMojo(MavenProject mavenProject, File sourceDirectory, File classesDirectory) {
		this.mavenProject = mavenProject;
		this.sourceDirectory = sourceDirectory;
		this.classesDirectory = classesDirectory;
	}

	@Override
	public void execute() throws MojoExecutionException {
		log.banner("QUERYDSL META MODEL GENERATION START");

		log.info("Config: " + config);
		ensureDirectoryExists(sourceDirectory, "Source");
		ensureDirectoryExists(classesDirectory, "Classes");

		generateMetaModel();
		scanMetaModelSources();
		compileMetaModelSources();

		log.info("QueryDSL SQL meta-model class generation and compilation finished successfully.");
		log.banner("QUERYDSL META MODEL GENERATION END");
	}

	private void generateMetaModel() {
		QueryDslSqlMetaModelGenerator metaModelGenerator = new QueryDslSqlMetaModelGenerator(log, config);
		metaModelGenerator.generate();
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
		log.step("Scanning generated metamodel sources in: " + sourceDirectory.getAbsolutePath());
		FileSet fileSet = new FileSet();
		fileSet.setDirectory(sourceDirectory.getAbsolutePath());
		fileSet.addInclude("**/*.java");

		FileSetManager fileSetManager = new FileSetManager();
		String[] files = fileSetManager.getIncludedFiles(fileSet);
		for (String fileName : files) {
			File candidateFile = new File(sourceDirectory, fileName);
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
					String errorMsg = "Compilation failed for " + sourceFile.getName() + ":\n" + errStream.toString(StandardCharsets.UTF_8);
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

	@VisibleForTesting
	List<File> getMetaModelSources() {
		return metaModelSources;
	}
}
