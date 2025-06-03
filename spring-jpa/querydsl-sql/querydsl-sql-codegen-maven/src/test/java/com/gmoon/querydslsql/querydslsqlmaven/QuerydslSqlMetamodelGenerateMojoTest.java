/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package com.gmoon.querydslsql.querydslsqlmaven;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class QuerydslSqlMetamodelGenerateMojoTest {

	private final Deque<String> logMessages = new ArrayDeque<>();

	@TempDir
	private File tempDir;

	private File metaClass;
	private QuerydslSqlMetamodelGenerateMojo mojo;

	@BeforeEach
	void setUp() throws Exception {
		File sourceDirectory = makeDirectory(tempDir, "generated-sources", "java");
		File classesDirectory = makeDirectory(tempDir, "classes");
		File metaFolder = makeDirectory(sourceDirectory, "com", "meta");

		String qDomainFilename = "SQueryDslSQLDomain.java";
		metaClass = Files.copy(
			 Paths.get("build/enhance/src/main/java/" + qDomainFilename),
			 createFile(metaFolder, qDomainFilename).toPath(),
			 StandardCopyOption.REPLACE_EXISTING
		).toFile();

		File barFolder = makeDirectory(sourceDirectory, "com", "foo", "bar");
		createFile(barFolder, "Boo.txt");

		MavenProject mavenProject = Mockito.spy(MavenProject.class);
		Mockito.doReturn(Arrays.asList(
			 getAbsolutePath("lib/querydsl-core-5.1.0.jar"),
			 getAbsolutePath("lib/querydsl-sql-5.1.0.jar")
		)).when(mavenProject).getCompileClasspathElements();

		mojo = new QuerydslSqlMetamodelGenerateMojo(
			 mavenProject,
			 sourceDirectory,
			 classesDirectory
		);
		mojo.setLog(createLog());
	}

	@AfterEach
	void tearDown() {
		printLogMessages();
	}

	@Test
	void scanMetaModelSources() throws Exception {
		invokePrivateMethod("scanMetaModelSources");

		assertThat(mojo.getMetaModelSources())
			 .hasSize(1)
			 .containsExactly(metaClass);
	}

	@Test
	void compileMetaModelSources() throws Exception {
		scanMetaModelSources();

		assertThatCode(() -> invokePrivateMethod("compileMetaModelSources"))
			 .doesNotThrowAnyException();
	}

	private void invokePrivateMethod(String methodName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Method method = QuerydslSqlMetamodelGenerateMojo.class.getDeclaredMethod(methodName);
		method.setAccessible(true);
		method.invoke(mojo);
	}

	private File makeDirectory(File baseDir, String... subPaths) throws IOException {
		Path fullPath = baseDir.toPath();
		for (String part : subPaths) {
			fullPath = fullPath.resolve(part);
		}
		Files.createDirectories(fullPath);
		return fullPath.toFile();
	}

	private File createFile(File targetDir, String fileName) throws IOException {
		Path dirPath = targetDir.toPath();
		if (!Files.exists(dirPath)) {
			Files.createDirectories(dirPath);
		}
		Path filePath = dirPath.resolve(fileName);
		Files.createFile(filePath);
		return filePath.toFile();
	}

	private String getAbsolutePath(String filePath) throws URISyntaxException {
		URL url = getClass().getClassLoader().getResource(filePath);
		if (url == null) {
			throw new IllegalArgumentException("Resource not found: " + filePath);
		}
		return Paths.get(url.toURI()).toAbsolutePath().toString();
	}

	private void printLogMessages() {
		while (!logMessages.isEmpty()) {
			System.out.println(logMessages.poll());
		}
	}

	private Log createLog() {
		return (Log) Proxy.newProxyInstance(
			 getClass().getClassLoader(),
			 new Class[]{Log.class},
			 (proxy, method, args) -> {
				 if ("info".equals(method.getName())) {
					 logMessages.offer("[INFO] " + args[0]);
				 } else if ("warn".equals(method.getName())) {
					 logMessages.offer("[WARNING] " + args[0]);
				 } else if ("error".equals(method.getName())) {
					 logMessages.offer("[ERROR] " + args[0]);
				 } else if ("debug".equals(method.getName())) {
					 logMessages.offer("[DEBUG] " + args[0]);
				 }
				 return null;
			 });
	}
}
