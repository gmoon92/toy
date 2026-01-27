package com.gmoon.springcloudbus.springcloudbusclient.listener;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cloud.bus.event.AckRemoteApplicationEvent;
import org.springframework.cloud.bus.event.RefreshRemoteApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BusEventListener {

	@EventListener
	public void onRefreshRemoteApplicationEvent(RefreshRemoteApplicationEvent event) {
		Map<String, Object> details = new LinkedHashMap<>();
		details.put("Event Type", "CONFIGURATION REFRESH");
		details.put("Origin Service", event.getOriginService());
		details.put("Destination Service", event.getDestinationService());
		details.put("Event ID", event.getId());
		details.put("Timestamp", LocalDateTime.now());

		logBusEvent("REFRESH EVENT RECEIVED", details,
			"→ @RefreshScope beans will be recreated",
			"→ @ConfigurationProperties will be rebound",
			"→ Latest config will be fetched from Config Server"
		);
	}

	@EventListener
	public void onAckRemoteApplicationEvent(AckRemoteApplicationEvent event) {
		if (isFromThisInstance(event)) {
			Map<String, Object> details = new LinkedHashMap<>();
			details.put("Event Type", "CONFIGURATION REFRESH COMPLETED");
			details.put("Ack ID", event.getAckId());
			details.put("Ack Destination", event.getAckDestinationService());
			details.put("Destination", event.getDestinationService());
			details.put("Source", event.getSource());
			details.put("Origin service", event.getOriginService());
			details.put("Timestamp", LocalDateTime.now());

			logBusEvent("REFRESH COMPLETED", details,
				"✓ Configuration refresh completed successfully",
				"✓ Application is now using the latest configuration"
			);
		}
	}

	private boolean isFromThisInstance(AckRemoteApplicationEvent event) {
		String ackDestination = event.getAckDestinationService();
		return ackDestination != null && !ackDestination.contains(":**");
	}

	private void logBusEvent(String title, Map<String, Object> details, String... messages) {
		String separator = "═".repeat(55);

		String detailsLog = details.entrySet().stream()
			.map(entry -> String.format("%-23s : %s", entry.getKey(), entry.getValue()))
			.collect(Collectors.joining("\n"));

		StringBuilder logMessage = new StringBuilder()
			.append("\n╔").append(separator).append("╗")
			.append("\n║  ").append(String.format("%-51s", title)).append("  ║")
			.append("\n╚").append(separator).append("╝")
			.append("\n").append(detailsLog);

		if (messages.length > 0) {
			logMessage.append("\n");
			for (String message : messages) {
				logMessage.append("\n").append(message);
			}
		}

		logMessage.append("\n").append(separator);

		log.info(logMessage.toString());
	}
}
