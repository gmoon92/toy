package com.gmoon.javacore.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.util.UUID;

public final class FileUtils {

	public static File getResourceFile(String fileNameWithResourceFilePath) {
		URI resourceUri = getResourceUri(fileNameWithResourceFilePath);
		return new File(resourceUri);
	}

	private static URI getResourceUri(String fileName) {
		try {
			ClassLoader classLoader = FileUtils.class.getClassLoader();
			URL resource = classLoader.getResource(fileName);
			return resource.toURI();
		} catch (Exception e) {
			throw new IllegalArgumentException(fileName + "is not found", e);
		}
	}

	public static InputStream convertFileToInputStream(File file) {
		try {
			return org.apache.commons.io.FileUtils.openInputStream(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static File convertInputStreamToFile(InputStream in) {
		try {
			File tempFile = createTempFile();
			copyInputStreamToFile(in, tempFile);
			return tempFile;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static File createTempFile() {
		return createTempFile(null);
	}

	private static File createTempFile(File file) {
		try {
			File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".tmp", file);
			tempFile.deleteOnExit();

			return tempFile;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void copyInputStreamToFile(InputStream inputStream, File file) throws IOException {
		org.apache.commons.io.FileUtils.copyInputStreamToFile(inputStream, file);
	}

	public static String convertFileToString(File file) {
		try (
			InputStream fis = new FileInputStream(file);
			InputStream dis = new DataInputStream(fis)
		) {
			return toStringFromInputStream(dis);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static String toStringFromInputStream(InputStream inputStream) {
		try (
			Reader reader = new InputStreamReader(inputStream);
			BufferedReader br = new BufferedReader(reader)
		) {
			StringBuilder builder = new StringBuilder();

			String line;
			while (( line = br.readLine() ) != null) {
				builder.append(line).append("\n");
			}

			return builder.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
