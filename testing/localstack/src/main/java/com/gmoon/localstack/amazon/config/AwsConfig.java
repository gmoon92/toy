package com.gmoon.localstack.amazon.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.gmoon.localstack.amazon.constants.AmazonRegion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AwsConfig {

	@Bean
	public AmazonS3 amazonS3(AmazonProperty property) {
		AmazonProperty.S3 s3 = property.s3;
		AmazonRegion region = s3.region;
		String accessKey = s3.accessKey;
		String secretKey = s3.secretKey;

		log.debug("amazon s3. region: {}, access key: {}, secret key: {}", region, accessKey, secretKey);
		return AmazonS3ClientBuilder.standard()
			.withRegion(region.getValue())
			.withCredentials(awsCredentialsProvider(accessKey, secretKey))
			.build();
	}

	private AWSCredentialsProvider awsCredentialsProvider(String awsAccessKey, String awsSecretKey) {
		AWSCredentials credentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
		return new AWSStaticCredentialsProvider(credentials);
	}
}
