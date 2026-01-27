package com.gmoon.springcloudbus.springcloudbusclient.listener;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.gmoon.springcloudbus.common.events.UserLoginEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomEventListener {

	private final Map<String, LoginUserInfo> recentLogins = new ConcurrentHashMap<>();

	@EventListener
	public void onUserLoginEvent(UserLoginEvent event) {
		log.info("╔═══════════════════════════════════════════════════════╗");
		log.info("║         CUSTOM BUS EVENT RECEIVED                     ║");
		log.info("╚═══════════════════════════════════════════════════════╝");
		log.info("Event Type: UserLoginEvent");
		log.info("Username: {}", event.getUsername());
		log.info("IP Address: {}", event.getIpAddress());
		log.info("Timestamp: {}", LocalDateTime.ofInstant(
			Instant.ofEpochMilli(event.getTimestamp()),
			ZoneId.systemDefault()
		));
		log.info("Origin Service: {}", event.getOriginService());
		log.info("Destination Service: {}", event.getDestinationService());
		log.info("═══════════════════════════════════════════════════════");

		LoginUserInfo loginUserInfo = new LoginUserInfo(
			event.getUsername(),
			event.getIpAddress(),
			event.getTimestamp(),
			event.getOriginService()
		);
		recentLogins.put(event.getUsername(), loginUserInfo);

		clearUserCache(event.getUsername());
		updateLoginMetrics(event.getUsername(), event.getIpAddress());
		checkSecurityAlerts(event.getUsername(), event.getIpAddress());

		log.info("UserLoginEvent processed successfully for user: {}", event.getUsername());
	}

	private void clearUserCache(String username) {
		log.debug("Clearing cache for user: {}", username);
	}

	private void updateLoginMetrics(String username, String ipAddress) {
		log.debug("Updating login metrics for user: {} from IP: {}", username, ipAddress);
	}

	private void checkSecurityAlerts(String username, String ipAddress) {
		log.debug("Checking security alerts for user: {} from IP: {}", username, ipAddress);
	}

	public Map<String, LoginUserInfo> getRecentLogins() {
		return Map.copyOf(recentLogins);
	}
}
