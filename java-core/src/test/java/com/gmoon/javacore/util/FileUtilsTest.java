package com.gmoon.javacore.util;

import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

@DisplayName("파일 유틸 클래스")
class FileUtilsTest {

	private String fileNameWithResourceFilePath;

	@BeforeEach
	void setUp() {
		String fileName = "key.properties";
		fileNameWithResourceFilePath = String.format("src/test/resources/security/rsa/%s", fileName);
	}

	@DisplayName("resource 파일 읽기")
	@Test
	void getResourceFile() {
		// when then
		assertThat(FileUtils.getResourceFile(fileNameWithResourceFilePath))
			 .isInstanceOf(File.class);
	}

	@DisplayName("파일 변환")
	@Nested
	class ConvertTest {

		@DisplayName("InputStream -> File")
		@Test
		void convertInputStreamToFile() {
			// given
			File file = FileUtils.getResourceFile(fileNameWithResourceFilePath);

			// when
			InputStream is = FileUtils.convertFileToInputStream(file);

			// then
			assertThat(FileUtils.convertInputStreamToFile(is))
				 .isInstanceOf(File.class);
		}

		@DisplayName("File -> InputStream")
		@Test
		void convertFileToInputStream() {
			// given
			File file = FileUtils.getResourceFile(fileNameWithResourceFilePath);

			// when then
			assertThat(FileUtils.convertFileToInputStream(file))
				 .isInstanceOf(InputStream.class);
		}

		@DisplayName("File -> String")
		@Test
		void convertFileToString() {
			// given
			File file = FileUtils.getResourceFile(fileNameWithResourceFilePath);

			// when then
			assertThat(FileUtils.convertFileToString(file))
				 .isInstanceOf(String.class);
		}

		@DisplayName("MultipartFile -> File")
		@Test
		void convertFileToMultipartFile() throws IOException {
			// given
			File file = FileUtils.getResourceFile(fileNameWithResourceFilePath);
			MockMultipartFile multipartFile = new MockMultipartFile(
				 "file",
				 file.getName(),
				 MediaType.IMAGE_PNG_VALUE,
				 FileUtils.convertFileToInputStream(file)
			);

			// when then
			assertThat(FileUtils.convertFileToMultipartFile(multipartFile))
				 .isInstanceOf(File.class);
		}
	}
}
