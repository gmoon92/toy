package com.gmoon.localstack.aws;

import static org.assertj.core.api.Assertions.assertThat;
import com.gmoon.javacore.util.FileUtils;
import com.gmoon.localstack.aws.model.S3RequestVO;
import com.gmoon.localstack.aws.model.S3ResponseVO;
import com.gmoon.localstack.test.config.LocalStackS3Config;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

@Disabled
@SpringBootTest(classes = LocalStackS3Config.class)
class AmazonS3ServiceTest {

	final String BUCKET_NAME = "gmoon-local-bucket";

	@Autowired
	AmazonS3Service service;

	@DisplayName("MultipartFile S3 파일 업로드")
	@Test
	void upload() {
		// given
		File file = FileUtils.getResourceFile("s3/github.txt");

		S3RequestVO requestVO = S3RequestVO.builder()
			.bucketName(BUCKET_NAME)
			.key("resources/public")
			.multipartFile(createMultipartFile(file))
			.build();

		// when then
		assertThat(service.upload(requestVO))
			.isInstanceOf(S3ResponseVO.class);
	}

	private MockMultipartFile createMultipartFile(File file) {
		try {
			return new MockMultipartFile(
				"file",
				file.getName(),
				MediaType.IMAGE_PNG_VALUE,
				FileUtils.convertFileToInputStream(file)
			);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
