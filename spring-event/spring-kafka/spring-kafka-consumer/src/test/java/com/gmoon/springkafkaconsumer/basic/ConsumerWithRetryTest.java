package com.gmoon.springkafkaconsumer.basic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.*;


@Disabled
@Slf4j
@DisplayName("카프카 오프셋 커밋 실패 시 재시도 처리 테스트")
class ConsumerWithRetryTest {

	private final String topic = "sample";

	private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
	private final Map<TopicPartition, RetryData> retryOffsets = new HashMap<>();

	@Test
	void test() {
		try (Consumer<String, String> consumer = new KafkaConsumer<>(getProperties())) {
			consumer.subscribe(Collections.singletonList(topic));
			ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
			int count = 0;
			while (true) {
				for (ConsumerRecord<String, String> record : records) {
					log.info("Kafka Message Consumed - Topic: {}, Partition: {}, Offset: {}, Key: {}, Value: {}, Timestamp: {}",
						 record.topic(), record.partition(), record.offset(), record.key(), record.value(), record.timestamp());

					// 오프셋 저장 (다음 읽을 오프셋)
					currentOffsets.put(
						 new TopicPartition(record.topic(), record.partition()),
						 new OffsetAndMetadata(record.offset() + 1, "tx-id-001")
					);

					// 10개 단위로 비동기 오프셋 커밋
					if (count % 10 == 0) {
						consumer.commitAsync(currentOffsets, (offsets, exception) -> {
							if (exception != null) {
								// 예외 발생 시 로깅
								log.error("Async Commit Failed for Offsets: {} - Exception: {}", offsets, exception.getMessage());

								// 커밋 실패한 오프셋을 재시도 리스트에 저장
								for (TopicPartition partition : offsets.keySet()) {
									retryOffsets.put(partition, new RetryData(offsets.get(partition)));
								}

								// 예외가 발생한 오프셋에 대해서 재시도 로직 시작
								retryCommitOffsets(consumer);
							}
						});
					}
					count++;
				}
			}
		}
	}

	private void retryCommitOffsets(Consumer<String, String> consumer) {
		Iterator<Map.Entry<TopicPartition, RetryData>> iterator = retryOffsets.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<TopicPartition, RetryData> entry = iterator.next();
			TopicPartition partition = entry.getKey();
			RetryData retryData = entry.getValue();

			// 재시도 횟수 체크
			if (retryData.getRetryCount() < 3) {
				try {
					// 오프셋 커밋 시도
					consumer.commitAsync(Collections.singletonMap(partition, retryData.getOffset()), (offsets, exception) -> {
						if (exception != null) {
							// 예외가 발생하면 다시 재시도
							log.error("Retry Commit Failed for Offset: {} - Exception: {}", partition, exception.getMessage());

							// 재시도 횟수 증가
							retryOffsets.put(partition, retryData.increaseRetryCount());
						} else {
							// 재시도 성공 시, 재시도 리스트에서 제거
							iterator.remove();
							log.info("Retry Commit Success for offset: {}", retryData.getOffset().offset());
						}
					});

					var interval = 1_000L; // 재시도 간격 (밀리초)
					Thread.sleep(interval);
				} catch (InterruptedException e) {
					log.error("Error while retrying commit for offset: {}", retryData.getOffset().offset());
					Thread.currentThread().interrupt();
				}
			} else {
				// 재시도 횟수를 초과한 경우
				log.error("Max retry limit reached for offset: {}", retryData.getOffset().offset());
				iterator.remove();  // 재시도 리스트에서 제거
			}
		}
	}

	private Properties getProperties() {
		Properties props = new Properties();
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // 브로커 서버 지정
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-group01"); // 컨슈머 그룹 지정
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // 오토 커밋 비활성화
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); // 컨슈머 오프셋을 찾지 못한 경우 최근 메시지를 가져옴
		return props;
	}

	@AllArgsConstructor
	@Getter
	class RetryData {
		private OffsetAndMetadata offset;
		private int retryCount;

		public RetryData(OffsetAndMetadata offset) {
			this.offset = offset;
			this.retryCount = 1;
		}

		public RetryData increaseRetryCount() {
			return new RetryData(offset, retryCount + 1);
		}
	}
}
