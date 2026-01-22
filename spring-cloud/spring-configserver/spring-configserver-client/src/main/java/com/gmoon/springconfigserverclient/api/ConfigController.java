package com.gmoon.springconfigserverclient.api;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gmoon.springconfigserverclient.config.AppProperties;
import com.gmoon.springconfigserverclient.config.DatabaseProperties;

import lombok.RequiredArgsConstructor;

/**
 * 현재 설정 값을 확인하는 컨트롤러
 *
 * <pre>
 * 테스트 방법:
 * 1. Config Server와 Client 애플리케이션을 모두 실행
 * 2. http://localhost:8080/config 접속하여 현재 설정 값 확인
 * 3. config-repo/spring-configserver-client.yml 파일 수정
 * 4. POST http://localhost:8080/actuator/refresh 호출
 * 5. http://localhost:8080/config 다시 접속하여 변경된 설정 값 확인
 * </pre>
 */
@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class ConfigController {
	private final AppProperties appProperties;
	private final DatabaseProperties databaseProperties;

	@GetMapping
	public ConfigResponse getConfig() {
		return ConfigResponse.of(appProperties, databaseProperties);
	}

	@GetMapping("/message")
	public Map<String, String> getMessage() {
		Map<String, String> response = new HashMap<>();
		response.put("message", appProperties.getMessage());
		response.put("version", appProperties.getVersion());
		return response;
	}
}
