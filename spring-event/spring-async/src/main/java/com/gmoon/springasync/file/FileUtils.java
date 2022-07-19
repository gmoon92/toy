package com.gmoon.springasync.file;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileUtils {

	public static File getFileFromResource(String fileName) {
		URI resourceUri = getResourceUri(fileName);
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

	public static String convertFileToString(File file) {
		try {
			InputStream is = new DataInputStream(new FileInputStream(file));
			return readFromInputStream(is);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private static String readFromInputStream(InputStream inputStream) {
		StringBuilder resultStringBuilder = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
			String line;
			while (( line = br.readLine() ) != null) {
				resultStringBuilder.append(line).append("\n");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return resultStringBuilder.toString();
	}
}
