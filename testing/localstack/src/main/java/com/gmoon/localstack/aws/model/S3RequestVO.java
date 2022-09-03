package com.gmoon.localstack.aws.model;

import java.util.Objects;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class S3RequestVO {

	private final String bucketName;
	private final String key;
	private final MultipartFile multipartFile;

	@Builder
	private S3RequestVO(String bucketName, String key, MultipartFile multipartFile) {
		Objects.requireNonNull(bucketName);
		Objects.requireNonNull(key);
		Objects.requireNonNull(multipartFile);

		this.bucketName = bucketName;
		this.key = key;
		this.multipartFile = multipartFile;
	}
}
