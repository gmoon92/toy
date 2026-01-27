package com.gmoon.springcloudbus.springcloudbusclient.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gmoon.springcloudbus.springcloudbusclient.config.AppProperties;
import com.gmoon.springcloudbus.springcloudbusclient.config.DatabaseProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RefreshScope
@RestController
@RequiredArgsConstructor
public class ConfigController {
	private final AppProperties configProperties;
	private final DatabaseProperties databaseProperties;
	private final Environment environment;

	@Value("${app.config.message:Default from @Value}")
	private String messageFromValue;

	@Value("${spring.application.name}")
	private String applicationName;

	@Value("${server.port}")
	private String serverPort;

	@Value("${spring.cloud.bus.id}")
	private String busId;

	@GetMapping("/config")
	public Map<String, Object> getConfig() {
		log.info("Fetching current configuration for instance: {}", busId);

		Map<String, Object> response = new HashMap<>();

		response.put("instance", Map.of(
			 "applicationName", applicationName,
			 "serverPort", serverPort,
			 "busId", busId,
			 "lastAccessed", LocalDateTime.now().toString()
		));

		response.put("valueInjection", Map.of(
			 "message", messageFromValue,
			 "mechanism", "빈 생성 시 주입, RefreshScope로 빈 재생성 시 갱신",
			 "refreshScopeRequired", true
		));

		response.put("environment", Map.of(
			 "message", environment.getProperty("app.config.message", "Default from Environment"),
			 "mechanism", "호출 시마다 PropertySource 실시간 조회, 항상 최신 값",
			 "refreshScopeRequired", false
		));

		response.put("configProperties", Map.of(
			 "message", configProperties.getMessage(),
			 "refreshCount", configProperties.getRefreshCount(),
			 "featureEnabled", configProperties.getFeature().isEnabled(),
			 "mechanism", "ConfigurationPropertiesRebinder가 자동 재바인딩",
			 "refreshScopeRequired", false
		));

		response.put("databaseProperties", Map.of(
			 "minSize", databaseProperties.pool().minSize(),
			 "maxSize", databaseProperties.pool().maxSize(),
			 "connectionTimeout", databaseProperties.pool().connectionTimeout(),
			 "mechanism", "ConfigurationPropertiesRebinder가 자동 재바인딩 (Record 타입)",
			 "refreshScopeRequired", false
		));

		log.info("Current configuration: {}", response);
		return response;
	}
}
