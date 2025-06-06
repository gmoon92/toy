package com.gmoon.localstack.aws;

import java.io.File;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;

import com.gmoon.javacore.util.FileUtils;
import com.gmoon.localstack.aws.config.AwsProperty;
import com.gmoon.localstack.aws.model.S3DownloadLink;
import com.gmoon.localstack.aws.model.S3RequestVO;
import com.gmoon.localstack.aws.model.S3ResponseVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AmazonS3Service {

	private final AwsProperty awsProperty;
	private final TransferManager transferManager;

	public S3ResponseVO upload(S3RequestVO requestVO) {
		File file = uploadFileToAmazonS3(requestVO);

		S3DownloadLink downloadLink = S3DownloadLink.builder()
			 .region(awsProperty.s3.region)
			 .userResourceUri(requestVO.getKey())
			 .fileName(file.getName())
			 .build();

		return S3ResponseVO.ok(downloadLink.getValue());
	}

	private File uploadFileToAmazonS3(S3RequestVO requestVO) {
		MultipartFile multipartFile = requestVO.getMultipartFile();
		File file = FileUtils.convertFileToMultipartFile(multipartFile);

		transferManager.upload(
			 requestVO.getBucketName(),
			 requestVO.getKey(),
			 file
		);
		return file;
	}

	private ObjectMetadata getObjectMetadata(MultipartFile multipartFile) {
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentType(multipartFile.getContentType());
		metadata.setContentLength(multipartFile.getSize());
		return metadata;
	}
}
