package com.gmoon.localstack.aws.config;

import com.gmoon.localstack.aws.constants.AmazonRegion;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("aws")
@RequiredArgsConstructor
public class AwsProperty {

	public final S3 s3;

	@RequiredArgsConstructor
	public static class S3 {
		public final AmazonRegion region;
		public final String accessKey;
		public final String secretKey;
	}
}
