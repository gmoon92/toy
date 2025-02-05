package com.gmoon.payment.appstore;

import com.apple.itunes.storekit.model.Environment;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "service.payment.appstore")
public record AppStoreProperties(
	 AppStoreEnvironment environment,
	 String rootCertDir,
	 PrivateKey privateKey,
	 String bundleId,
	 String issuerId,
	 Long appAppleId
) {

	record PrivateKey(String id, String filePath) {
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
