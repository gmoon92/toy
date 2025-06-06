package com.gmoon.localstack.aws.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import com.gmoon.localstack.aws.constants.AmazonRegion;

import lombok.RequiredArgsConstructor;

@ConfigurationProperties("aws")
public class AwsProperty {

	public final S3 s3;

	@ConstructorBinding
	public AwsProperty(S3 s3) {
		this.s3 = s3;
	}

	@RequiredArgsConstructor
	public static class S3 {
		public final AmazonRegion region;
		public final String accessKey;
		public final String secretKey;
	}
}
