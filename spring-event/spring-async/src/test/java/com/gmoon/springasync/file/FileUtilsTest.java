package com.gmoon.springasync.file;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.File;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FileUtilsTest {


	@DisplayName("리소스 디렉토리 파일 읽기")
	@Test
	void getFileFromResource() {
		// given
		String fileName = "public/images/cat.png";

		// when
		File file = FileUtils.getFileFromResource(fileName);

		// then
		assertThat(file.exists()).isTrue();
	}

	@DisplayName("리소스 파일 문자열 변환")
	@Test
	void convertFileToString() {
		// given
		String fileName = "templates/invite.html";
		File file = FileUtils.getFileFromResource(fileName);

		// when
		String html = FileUtils.convertFileToString(file);

		// then
		assertThat(html).contains("</html>");
	}
}
