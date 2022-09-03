package com.gmoon.localstack.test;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import java.net.URI;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class LocalStackS3Config {

	private static final LocalStackContainer.Service S3 = LocalStackContainer.Service.S3;
	private static final String IMAGE_NAME = "localstack/localstack:0.14.3";

	@Bean(initMethod = "start", destroyMethod = "stop")
	public LocalStackContainer localStackContainer() {
		return new LocalStackContainer(
			DockerImageName.parse(IMAGE_NAME)
		).withServices(S3);
	}

	@Bean
	public AmazonS3 amazonS3(LocalStackContainer localstack) {
		String accessKey = localstack.getAccessKey();
		String secretKey = localstack.getSecretKey();

		return AmazonS3ClientBuilder.standard()
			.withCredentials(awsCredentialsProvider(accessKey, secretKey))
			.withEndpointConfiguration(localStackEndpointConfiguration(localstack))
			.build();
	}

	private AWSCredentialsProvider awsCredentialsProvider(String accessKey, String secretKey) {
		return new AWSStaticCredentialsProvider(
			new BasicAWSCredentials(accessKey, secretKey)
		);
	}

	private AwsClientBuilder.EndpointConfiguration localStackEndpointConfiguration(LocalStackContainer localstack) {
		URI endpoint = localstack.getEndpointOverride(S3);
		return new AwsClientBuilder.EndpointConfiguration(
			endpoint.toString(),
			localstack.getRegion()
		);
	}

	@Bean
	public TransferManager transferManager(AmazonS3 amazonS3) {
		return TransferManagerBuilder
			.standard()
			.withS3Client(amazonS3)
			.build();
	}
}
