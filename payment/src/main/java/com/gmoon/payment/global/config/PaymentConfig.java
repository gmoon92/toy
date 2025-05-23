package com.gmoon.payment.global.config;

import com.gmoon.payment.appstore.infra.AppStoreClient;
import com.gmoon.payment.global.properties.AppStoreProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

@Configuration
public class PaymentConfig {

	@Bean
	@ConditionalOnProperty(name = "service.payment.appstore.enabled", havingValue = "1")
	public AppStoreClient appStoreClient(AppStoreProperties properties, ResourceLoader resourceLoader) {
		return new AppStoreClient(properties, resourceLoader);
	}
}
