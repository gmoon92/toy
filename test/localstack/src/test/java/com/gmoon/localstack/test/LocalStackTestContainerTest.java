package com.gmoon.localstack.test;

import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.gmoon.javacore.util.FileUtils;

@Disabled("Testcontainers 로컬 테스트")
@Testcontainers
class LocalStackTestContainerTest {

	@Container
	final LocalStackContainer localstack = new LocalStackContainer(
		 DockerImageName.parse("localstack/localstack:0.14.3")
	);

	final AmazonS3 s3Client = createAmazonS3();

	private AmazonS3 createAmazonS3() {
		String accessKey = localstack.getAccessKey();
		String secretKey = localstack.getSecretKey();

		URI endpoint = localstack.getEndpointOverride(LocalStackContainer.Service.S3);
		return AmazonS3ClientBuilder.standard()
			 .withCredentials(awsCredentialsProvider(accessKey, secretKey))
			 .withEndpointConfiguration(
				  new AwsClientBuilder.EndpointConfiguration(
					   endpoint.toString(),
					   localstack.getRegion()
				  ))
			 .build();
	}

	private AWSCredentialsProvider awsCredentialsProvider(String accessKey, String secretKey) {
		return new AWSStaticCredentialsProvider(
			 new BasicAWSCredentials(accessKey, secretKey)
		);
	}

	@Bean
	public TransferManager transferManager(AmazonS3 amazonS3) {
		return TransferManagerBuilder
			 .standard()
			 .withS3Client(amazonS3)
			 .build();
	}

	@DisplayName("Amazon S3")
	@Nested
	class S3Test {

		final String BUCKET_NAME = "gmoon-local-bucket";

		@BeforeEach
		void setUp() {
			s3Client.createBucket(BUCKET_NAME);
		}

		@DisplayName("S3는 우선 버킷을 생성해야 한다.")
		@Test
		void createBucket() {
			// when
			List<String> bucketNames = s3Client.listBuckets().stream()
				 .map(Bucket::getName)
				 .toList();

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
