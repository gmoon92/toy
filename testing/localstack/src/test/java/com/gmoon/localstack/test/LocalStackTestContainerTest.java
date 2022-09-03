package com.gmoon.localstack.test;

import static org.assertj.core.api.Assertions.assertThat;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.gmoon.javacore.util.FileUtils;
import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Disabled("Testcontainers 로컬 테스트")
@Testcontainers
class LocalStackTestContainerTest {

	private static final LocalStackContainer.Service S3 = LocalStackContainer.Service.S3;

	@Container
	final LocalStackContainer localstack = new LocalStackContainer(
		DockerImageName.parse("localstack/localstack:0.14.3")
	).withServices(S3);

	final String BUCKET_NAME = "gmoon-local-bucket";
	AmazonS3 s3Client;

	@BeforeEach
	void setUp() {
		s3Client = createAmazonS3Client();
		s3Client.createBucket(BUCKET_NAME);
	}

	private AmazonS3 createAmazonS3Client() {
		String accessKey = localstack.getAccessKey();
		String secretKey = localstack.getSecretKey();

		return AmazonS3ClientBuilder.standard()
			.withCredentials(awsCredentialsProvider(accessKey, secretKey))
			.withEndpointConfiguration(getEndpointConfiguration())
			.build();
	}

	private AwsClientBuilder.EndpointConfiguration getEndpointConfiguration() {
		URI endpoint = localstack.getEndpointOverride(S3);
		return new AwsClientBuilder.EndpointConfiguration(
			endpoint.toString(),
			localstack.getRegion()
		);
	}

	private AWSCredentialsProvider awsCredentialsProvider(String accessKey, String secretKey) {
		AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
		return new AWSStaticCredentialsProvider(credentials);
	}

	@DisplayName("Amazon S3")
	@Nested
	class S3Test {

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
