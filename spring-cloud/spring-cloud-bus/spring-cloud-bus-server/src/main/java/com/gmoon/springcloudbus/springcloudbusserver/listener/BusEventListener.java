package com.gmoon.springcloudbus.springcloudbusserver.listener;

import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.bus.event.AckRemoteApplicationEvent;
import org.springframework.cloud.bus.event.RefreshRemoteApplicationEvent;
import org.springframework.cloud.bus.event.SentApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BusEventListener {

	@EventListener
	public void onSentApplicationEvent(SentApplicationEvent event) {
		logBusEvent("SENT", Map.of(
			"Origin Service", event.getOriginService(),
			"Destination Service", event.getDestinationService(),
			"Event ID", event.getId(),
			"Type", event.getType()
		));
	}

	@EventListener
	public void onAckRemoteApplicationEvent(AckRemoteApplicationEvent event) {
		logBusEvent("ACK", Map.of(
			"Origin Service", event.getOriginService(),
			"Destination Service", event.getDestinationService(),
			"Event ID", event.getAckId(),
			"Ack Destination", event.getAckDestinationService()
		));
	}

	@EventListener
	public void onRefreshRemoteApplicationEvent(RefreshRemoteApplicationEvent event) {
		logBusEvent("REFRESH", Map.of(
			"Origin Service", event.getOriginService(),
			"Destination Service", event.getDestinationService(),
			"Event ID", event.getId()
		));
	}

	private void logBusEvent(String eventType, Map<String, Object> details) {
		String separator = "=".repeat(50);
		String title = String.format("Bus Event [%s]", eventType);

		String detailsLog = details.entrySet().stream()
			.map(entry -> String.format("  %-20s : %s", entry.getKey(), entry.getValue()))
			.collect(Collectors.joining("\n"));

		log.info("\n{}\n{}\n{}\n{}", separator, title, detailsLog, separator);
	}
}
