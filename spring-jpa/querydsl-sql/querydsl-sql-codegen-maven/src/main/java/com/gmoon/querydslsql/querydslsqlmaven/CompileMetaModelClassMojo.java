package com.gmoon.querydslsql.querydslsqlmaven;

import com.google.common.annotations.VisibleForTesting;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
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
 * Maven mojo for performing build-time enhancement of entity objects.
 */
@Mojo(
	 name = "compile-meta",
	 defaultPhase = LifecyclePhase.COMPILE,
	 requiresDependencyResolution = ResolutionScope.COMPILE
)
public class CompileMetaModelClassMojo extends AbstractMojo {

	private final List<File> metaModelSources = new ArrayList<>();

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject mavenProject;

	@Parameter(defaultValue = "${project.build.directory}/generated-sources/java/querydslmeta", readonly = true, required = true)
	private File sourceDirectory;

	@Parameter(defaultValue = "${project.build.directory}/classes", readonly = true, required = true)
	private File classesDirectory;

	@Parameter(defaultValue = "false")
	private boolean testClasspath;

	public CompileMetaModelClassMojo() {
	}

	public CompileMetaModelClassMojo(MavenProject mavenProject, File sourceDirectory, File classesDirectory) {
		this.mavenProject = mavenProject;
		this.sourceDirectory = sourceDirectory;
		this.classesDirectory = classesDirectory;
	}

	@Override
	public void execute() throws MojoExecutionException {
		getLog().info("[MetaModel] Compile Mojo execution started");
		ensureDirectoryExists(sourceDirectory, "Source");
		ensureDirectoryExists(classesDirectory, "Classes");

		scanMetaModelSources();
		compileMetaModelSources();
		metaModelSources.clear();

		getLog().debug("Ending execution of compile-meta mojo");
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
		getLog().info("[MetaModel] Scanning for metamodel sources under: " + sourceDirectory);
		FileSet fileSet = new FileSet();
		fileSet.setDirectory(sourceDirectory.getAbsolutePath());
		fileSet.addInclude("**/*.java");

		FileSetManager fileSetManager = new FileSetManager();
		String[] files = fileSetManager.getIncludedFiles(fileSet);
		for (String fileName : files) {
			File candidateFile = new File(sourceDirectory, fileName);
			if (candidateFile.isFile()) {
				metaModelSources.add(candidateFile);
				getLog().debug("[MetaModel] Found metamodel source: " + candidateFile.getAbsolutePath());
			}
		}
		getLog().info("[MetaModel] Total metamodel sources found: " + metaModelSources.size());
	}

	private void compileMetaModelSources() throws MojoExecutionException {
		if (metaModelSources.isEmpty()) {
			getLog().info("[MetaModel] No metamodel sources to compile.");
			return;
		}
		getLog().info("[MetaModel] Compilation started for " + metaModelSources.size() + " files.");
		for (File sourceFile : metaModelSources) {
			compileSourceFile(sourceFile);
		}
		getLog().info("[MetaModel] Compilation finished.");
	}

	private void compileSourceFile(File sourceFile) throws MojoExecutionException {
		getLog().info("[MetaModel] Compiling: " + sourceFile.getAbsolutePath());
		List<String> classpathElements = getClasspathElements();
		String classpath = classpathElements.isEmpty() ? "" : String.join(File.pathSeparator, classpathElements);

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			throw new MojoExecutionException("[MetaModel] JDK required for compilation.");
		}

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
				String errorMsg = "[MetaModel] Compilation failed for " + sourceFile.getName() + ":\n" + errStream.toString(StandardCharsets.UTF_8);
				getLog().error(errorMsg);
				throw new MojoExecutionException(errorMsg);
			}
		} catch (IOException e) {
			throw new MojoExecutionException("[MetaModel] Error compiling: " + sourceFile.getAbsolutePath(), e);
		}
	}

	private List<String> getClasspathElements() throws MojoExecutionException {
		try {
			if (testClasspath) {
				return mavenProject.getTestClasspathElements();
			}
			return mavenProject.getCompileClasspathElements();
		} catch (DependencyResolutionRequiredException e) {
			throw new MojoExecutionException("[MetaModel] Could not get classpath elements.", e);
		}
	}

	@VisibleForTesting
	List<File> getMetaModelSources() {
		return metaModelSources;
	}
}
