package com.gmoon.springconfigserverclient.api;

import com.gmoon.springconfigserverclient.config.AppProperties;
import com.gmoon.springconfigserverclient.config.DatabaseProperties;

public record ConfigResponse(App app, Database database) {
	public static ConfigResponse of(
		 AppProperties appProperties,
		 DatabaseProperties databaseProperties
	) {
		return new ConfigResponse(
			 new ConfigResponse.App(
				  appProperties.getMessage(),
				  appProperties.getVersion(),
				  new ConfigResponse.Feature(
					   appProperties.getFeature().isEnabled(),
					   appProperties.getFeature().getMaxRetry(),
					   appProperties.getFeature().getTimeoutSecond()
				  )
			 ),
			 new ConfigResponse.Database(
				  databaseProperties.pool().minSize(),
				  databaseProperties.pool().maxSize(),
				  databaseProperties.pool().connectionTimeout()
			 )
		);
	}

	public record App(
		 String message,
		 String version,
		 Feature feature
	) {
	}

	public record Feature(
		 boolean enabled,
		 int maxRetry,
		 int timeoutSecond
	) {
	}

	public record Database(
		 int minSize,
		 int maxSize,
		 int connectionTimeout
	) {
	}
}
