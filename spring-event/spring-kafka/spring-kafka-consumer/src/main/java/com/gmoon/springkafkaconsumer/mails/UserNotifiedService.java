package com.gmoon.springkafkaconsumer.mails;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserNotifiedService {

	private final MailHelper mailHelper;
	private final ProcessedEventRepository processedEventRepository;

	@KafkaListener(topics = "agent.disconnect", groupId = "service.mail.notified")
	@Transactional
	public void consume(String message) {
		DisconnectedAgentEvent event = getDisconnectedAgentEvent(message);

		// 1. 중복 이벤트 처리 방지
		boolean alreadyProcessed = processedEventRepository.exists(event.getEventId());
		if (alreadyProcessed) {
			return; // 멱등하게 처리: 이미 한 번 처리한 이벤트
		}

		// 2. 비즈니스 로직 수행
		sendMailDisconnectAgent();

		// 3. 처리 로그 저장 (이벤트 ID 기록)
		processedEventRepository.save(new ProcessedEvent(event.getEventId()));
	}




	private void sendMailDisconnectAgent() {
	}

	private DisconnectedAgentEvent getDisconnectedAgentEvent(String message) {
		return new DisconnectedAgentEvent();
	}
}
