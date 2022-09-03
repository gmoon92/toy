package com.gmoon.localstack.aws.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class S3ResponseVO {

	private static final String OK = "100";

	private final String status;
	private final String downloadUrl;

	private S3ResponseVO(String downloadUrl) {
		this(OK, downloadUrl);
	}

	public static S3ResponseVO ok(String downloadUrl) {
		return new S3ResponseVO(downloadUrl);
	}
}
