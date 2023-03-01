package com.gmoon.javacore.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

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
		return createTempFile(".tmp");
	}

	private static File createTempFile(String extension) {
		try {
			File tempFile = File.createTempFile(
				UUID.randomUUID().toString(),
				"." + extension,
				null
			);

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

	public static File convertFileToMultipartFile(MultipartFile multipartFile) {
		String filename = multipartFile.getOriginalFilename();
		String extension = FilenameUtils.getExtension(filename);

		File temp = createTempFile(extension);
		try (InputStream is = multipartFile.getInputStream()){
			copyInputStreamToFile(is, temp);
			multipartFile.transferTo(temp);
			return temp;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static File write(File target, String str) {
		try (FileOutputStream os = new FileOutputStream(target)){
			os.write(str.getBytes(StandardCharsets.UTF_8));
			os.flush();
			os.close();
			return target;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
