package com.gmoon.springcloudbus.springcloudbusserver.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.gmoon.springcloudbus.common.events.UserLoginEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BusEventController {

	private final ApplicationEventPublisher eventPublisher;

	@Value("${spring.cloud.bus.id}")
	private String busId;

	@PostMapping("/events/logins")
	public ResponseEntity<Map<String, Object>> publishUserLoginEvent(@RequestBody Map<String, String> payload) {
		String username = payload.get("username");
		String ipAddress = payload.get("ipAddress");

		log.info("Publishing UserLoginEvent for user: {} from IP: {}", username, ipAddress);

		eventPublisher.publishEvent(new UserLoginEvent(this, busId, "**:**", username, ipAddress));

		log.info("UserLoginEvent published successfully");

		return ResponseEntity.ok(Map.of(
			 "status", "success",
			 "message", "UserLoginEvent published to all instances",
			 "event", Map.of(
				  "username", username,
				  "ipAddress", ipAddress,
				  "originService", busId,
				  "destinationService", "**:**"
			 )
		));
	}
}
