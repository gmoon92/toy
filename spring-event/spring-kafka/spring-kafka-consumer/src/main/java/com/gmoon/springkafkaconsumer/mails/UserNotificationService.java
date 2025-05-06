package com.gmoon.springkafkaconsumer.mails;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gmoon.springkafkaconsumer.events.ProcessedEvent;
import com.gmoon.springkafkaconsumer.events.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserNotificationService {

	private final ProcessedEventRepository processedEventRepository;
	private final ObjectMapper objectMapper;

	@KafkaListener(topics = "outbox.event.notice", groupId = "service.mail.notified")
	@Transactional
	public void processEvent(
		 @Header("id") String eventId,
		 @Header("event_type") String eventType,
		 @Payload String payload
	) {
		log.info("Notification Event Consumed eventId: {}, eventType: {}, payload: {}", eventId, eventType, payload);

		if (processedEventRepository.existsById(eventId)) {
			log.debug("Already processed event: {}", eventId);
			return;
		}

		handleEvent(eventType, payload);

		processedEventRepository.save(new ProcessedEvent(eventId));
	}

	private void handleEvent(String eventType, String payload) {
		switch (eventType) {
			case "like-increased" -> log.info("increased like {}", payload);
			default -> log.warn("Unhandled event type: {}, event: {}", eventType, payload);
		}
	}
}
