package com.gmoon.outboxinvalidate.cache;

import java.time.Duration;
import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.outboxinvalidate.config.ServiceCacheProperties;

import lombok.RequiredArgsConstructor;

/**
 * 대기 중인 무효화 지시를 브로커로 옮긴다.
 * <p>
 * 브로커가 받았다고 확인한 뒤에만 행을 지운다. 보낸 뒤 삭제를 커밋하기 전에 죽으면 같은 지시를 다시 보낸다.
 * 캐시 삭제는 몇 번을 해도 결과가 같으므로 중복을 걸러 내지 않는다.
 */
@Component
@RequiredArgsConstructor
public class EvictionRelay {

	private static final Duration BROKER_CONFIRM_TIMEOUT = Duration.ofSeconds(5);

	private final PendingEvictionRepository pendingEvictionRepository;
	private final RabbitTemplate rabbitTemplate;
	private final ServiceCacheProperties properties;

	@Transactional
	public int relay() {
		ServiceCacheProperties.Outbox outbox = properties.outbox();
		List<PendingEviction> pendingEvictions = pendingEvictionRepository.findAllByOrderByIdAsc(
			 Limit.of(outbox.relayBatchSize())
		);
		if (pendingEvictions.isEmpty()) {
			return 0;
		}

		sendConfirmed(outbox.queue(), pendingEvictions);
		pendingEvictionRepository.deleteAllInBatch(pendingEvictions);
		return pendingEvictions.size();
	}

	private void sendConfirmed(String queue, List<PendingEviction> pendingEvictions) {
		rabbitTemplate.invoke(operations -> {
			pendingEvictions.forEach(eviction -> operations.convertAndSend(queue, eviction.cacheKey()));
			operations.waitForConfirmsOrDie(BROKER_CONFIRM_TIMEOUT.toMillis());
			return pendingEvictions.size();
		});
	}
}
