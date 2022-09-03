package com.gmoon.localstack.amazon.config;

import com.gmoon.localstack.amazon.constants.AmazonRegion;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("amazon")
@RequiredArgsConstructor
public class AmazonProperty {

	public final S3 s3;

	@RequiredArgsConstructor
	public static class S3 {
		public final AmazonRegion region;
		public final String accessKey;
	}
}
