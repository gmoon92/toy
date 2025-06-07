package com.gmoon.querydslsql.querydslsqlmaven;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import com.gmoon.querydslsql.querydslsqlmaven.logging.ConsoleLog;

public final class JavaCompilerHelper {
	private static final ConsoleLog log = new ConsoleLog("COMPILER");

	private MavenProject mavenProject;
	private List<String> compilerOptions;
	private boolean useTestClasspath;

	public JavaCompilerHelper(MavenProject mavenProject, List<String> compilerOptions, boolean useTestClasspath) {
		this.mavenProject = mavenProject;
		this.compilerOptions = compilerOptions;
		this.useTestClasspath = useTestClasspath;
	}

	private List<String> getClasspathElements() throws MojoExecutionException {
		try {
			if (useTestClasspath) {
				return mavenProject.getTestClasspathElements();
			}
			return mavenProject.getCompileClasspathElements();
		} catch (Exception e) {
			throw new MojoExecutionException("Could not get classpath elements.", e);
		}
	}

	public List<File> findJavaFiles(File dir) {
		List<File> result = new ArrayList<>();
		collectJavaFiles(dir, result);
		return result;
	}

	private void collectJavaFiles(File dir, List<File> found) {
		if (dir == null || !dir.exists()) {
			return;
		}

		File[] files = dir.listFiles();
		if (files == null) {
			return;
		}

		for (File file : files) {
			if (file.isDirectory()) {
				collectJavaFiles(file, found);
			} else if (file.getName().endsWith(".java")) {
				found.add(file);
			}
		}
	}

	public void compileJavaFiles(List<File> javaFiles, File outputDir) throws MojoExecutionException {
		if (!outputDir.exists()) {
			outputDir.mkdirs();
		}

		if (javaFiles.isEmpty()) {
			log.info("No java file found. Skipping compile step.");
			return;
		}

		log.info("Compiling " + javaFiles.size() + " source files...");
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			throw new MojoExecutionException("JDK required for source compilation (no javac found).");
		}

		String[] options = getJavacOptions(javaFiles, outputDir);
		try (ByteArrayOutputStream err = new ByteArrayOutputStream()) {
			int result = compiler.run(
				 null,
				 null,
				 err,
				 options
			);
			if (result != 0) {
				throw new MojoExecutionException("Compile failed:\n" + err.toString(StandardCharsets.UTF_8));
			}
		} catch (IOException e) {
			throw new MojoExecutionException("Error compiling files", e);
		}
	}

	private String[] getJavacOptions(List<File> javaFiles, File outputDir) throws MojoExecutionException {
		List<String> classpathElements = getClasspathElements();
		String classpath = classpathElements.isEmpty() ? "" : String.join(File.pathSeparator, classpathElements);
		log.step("Compiling classpath...." + classpath);

		List<String> options = new ArrayList<>(Arrays.asList(
			 "-d", outputDir.getAbsolutePath(),
			 "-classpath", classpath,
			 "-processorpath", classpath
		));

		if (compilerOptions != null && !compilerOptions.isEmpty()) {
			options.addAll(compilerOptions);
		}

		List<String> fileNames = javaFiles.stream()
			 .map(File::getAbsolutePath)
			 .collect(Collectors.toList());
		options.addAll(fileNames);

		log.info("Javac args: " + options);
		return options.toArray(new String[0]);
	}

	public void registerClassLoader(File classDirectory) throws MojoExecutionException {
		try {
			URLClassLoader cl = new URLClassLoader(new URL[] {classDirectory.toURI().toURL()},
				 getClass().getClassLoader());
			Thread.currentThread().setContextClassLoader(cl);
		} catch (Exception e) {
			throw new MojoExecutionException("Failed to make class loader", e);
		}
	}
}
