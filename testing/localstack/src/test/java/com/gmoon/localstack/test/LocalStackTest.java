package com.gmoon.localstack.test;

import static org.assertj.core.api.Assertions.assertThat;
import cloud.localstack.ServiceName;
import cloud.localstack.awssdkv1.TestUtils;
import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.gmoon.javacore.util.FileUtils;
import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@Disabled("localstack 로컬 테스트")
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(
	services = ServiceName.S3,
	portEdge = "5001"
)
class LocalStackTest {

	@DisplayName("Amazon S3")
	@Nested
	class S3Test {

		final String BUCKET_NAME = "gmoon-local-bucket";
		AmazonS3 s3Client;

		@BeforeEach
		void setUp() {
			s3Client = TestUtils.getClientS3();
			s3Client.createBucket(BUCKET_NAME);
		}

		@DisplayName("S3는 우선 버킷을 생성해야 한다.")
		@Test
		void createBucket() {
			// when
			List<String> bucketNames = s3Client.listBuckets().stream()
				.map(Bucket::getName)
				.collect(Collectors.toList());

			// then
			assertThat(bucketNames)
				.contains(BUCKET_NAME);
		}

		@DisplayName("S3에 지정된 버킷으로 파일을 업로드한다.")
		@Test
		void uploadFile() {
			// given
			String fileName = "s3/github.txt";

			File file = FileUtils.getResourceFile(fileName);
			String key = UUID.randomUUID().toString();
			s3Client.putObject(BUCKET_NAME, key, file);

			// when
			S3ObjectInputStream is = s3Client.getObject(BUCKET_NAME, key)
				.getObjectContent();

			// then
			File actual = FileUtils.convertInputStreamToFile(is);
			assertThat(FileUtils.convertFileToString(actual))
				.contains("github");
		}
	}
}
