package com.gmoon.localstack.aws.model;

import java.util.Objects;

import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.gmoon.javacore.util.StringUtils;
import com.gmoon.localstack.aws.constants.AmazonRegion;

import lombok.Builder;
import lombok.Getter;

@Getter
public class S3DownloadLink {

	private final String value;

	@Builder
	public S3DownloadLink(String bucketName,
		 AmazonRegion region,
		 String userResourceUri,
		 String fileName
	) {
		Objects.requireNonNull(bucketName);
		Objects.requireNonNull(region);
		Objects.requireNonNull(userResourceUri);
		Objects.requireNonNull(fileName);

		UriComponents components = UriComponentsBuilder
			 .fromUriString(String.format("https://%s.s3.%s.amazonaws.com", bucketName, region.getValue()))
			 .pathSegment(userResourceUri, fileName)
			 .build();

		value = toUriString(components);
	}

	private String toUriString(UriComponents components) {
		return getOrigin(components)
			 + cleanDoubleSlashPath(components.getPath());
	}

	private String getOrigin(UriComponents components) {
		String scheme = components.getScheme();
		String host = components.getHost();

		String result = scheme + "://" + host;
		int port = components.getPort();
		if (port == -1) {
			return result;
		}

		return result + ":" + port;
	}

	private String cleanDoubleSlashPath(String path) {
		final String doubleSlash = "//";
		path = StringUtils.replace(path, doubleSlash, "/");
		if (StringUtils.contains(path, doubleSlash)) {
			return cleanDoubleSlashPath(path);
		}

		return path;
	}
}
