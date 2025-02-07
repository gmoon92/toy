package com.gmoon.payment.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.apple.itunes.storekit.model.Environment;

import lombok.RequiredArgsConstructor;

@ConfigurationProperties(prefix = "service.payment.appstore")
public record AppStoreProperties(
	 AppStoreEnvironment environment,
	 PrivateKey privateKey,
	 Long appAppleId,
	 String bundleId,
	 String issuerId,
	 String rootCertDir
) {

	public record PrivateKey(String id, String filePath) {
	}

	public Environment getEnvironment() {
		return environment.value;
	}

	@RequiredArgsConstructor
	enum AppStoreEnvironment {
		SANDBOX(Environment.SANDBOX),
		PRODUCTION(Environment.PRODUCTION),
		XCODE(Environment.XCODE),
		LOCAL_TESTING(Environment.LOCAL_TESTING); // Used for unit testing

		private final Environment value;
	}
}
