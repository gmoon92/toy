package com.gmoon.payment.appstore;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "service.payment.appstore")
public record AppStoreProperties(
	 AppStoreEnvironment environment,
	 PrivateKey privateKey,
	 String bundleId,
	 String issuerId,
	 String rootCaDirectoryPath
) {

	record PrivateKey(String id, String filePath) {
	}
}
