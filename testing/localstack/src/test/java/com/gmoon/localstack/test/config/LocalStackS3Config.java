package com.gmoon.localstack.test.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.gmoon.localstack.test.annotation.EnableAwsLocalStack;
import java.util.Map;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.localstack.LocalStackContainer;

@EnableAwsLocalStack(LocalStackContainer.Service.S3)
@TestConfiguration
public class LocalStackS3Config {

	@Bean
	public AmazonS3 amazonS3(LocalStackContainer localstack,
							 Map<LocalStackContainer.Service, AwsClientBuilder.EndpointConfiguration> localStackEndpointConfigurations)
	{
		String accessKey = localstack.getAccessKey();
		String secretKey = localstack.getSecretKey();

		return AmazonS3ClientBuilder.standard()
			.withCredentials(awsCredentialsProvider(accessKey, secretKey))
			.withEndpointConfiguration(localStackEndpointConfigurations.get(LocalStackContainer.Service.S3))
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
}
