package com.gmoon.springcloudbus.springcloudbusclient.controller;

import com.gmoon.springcloudbus.springcloudbusclient.listener.CustomEventListener;
import com.gmoon.springcloudbus.springcloudbusclient.listener.LoginUserInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class BusEventController {

	private final CustomEventListener eventListener;

	@Value("${spring.cloud.bus.id}")
	private String busId;

	@GetMapping("/logins")
	public Map<String, Object> getLoginHistory() {
		log.info("Fetching login history for instance: {}", busId);

		Map<String, LoginUserInfo> recentLogins = eventListener.getRecentLogins();

		List<Map<String, Object>> loginList = recentLogins.values().stream()
			 .map(login -> Map.<String, Object>of(
				  "username", login.username(),
				  "ipAddress", login.ipAddress(),
				  "timestamp", LocalDateTime.ofInstant(
					   Instant.ofEpochMilli(login.timestamp()),
					   ZoneId.systemDefault()
				  ).toString(),
				  "originService", login.originService()
			 ))
			 .toList();

		return Map.of(
			 "instanceInfo", Map.of(
				  "busId", busId,
				  "totalLogins", loginList.size()
			 ),
			 "loginHistory", loginList,
			 "note", "This login history is synchronized across all instances via Spring Cloud Bus"
		);
	}

	@GetMapping("/logins/stats")
	public Map<String, Object> getLoginStats() {
		Map<String, LoginUserInfo> recentLogins = eventListener.getRecentLogins();

		long uniqueUsers = recentLogins.values().stream()
			 .map(LoginUserInfo::username)
			 .distinct()
			 .count();

		long uniqueIpAddresses = recentLogins.values().stream()
			 .map(LoginUserInfo::ipAddress)
			 .distinct()
			 .count();

		return Map.of(
			 "instance", busId,
			 "statistics", Map.of(
				  "totalLogins", recentLogins.size(),
				  "uniqueUsers", uniqueUsers,
				  "uniqueIpAddresses", uniqueIpAddresses
			 ),
			 "synchronization", Map.of(
				  "method", "Spring Cloud Bus",
				  "transport", "RabbitMQ",
				  "consistency", "Eventually Consistent"
			 )
		);
	}
}
