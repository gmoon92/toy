package com.gmoon.springkafka.basic.consumer;

import com.gmoon.springkafka.test.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * @apiNote <pre>
 * # 컨슈머 수동 커밋
 * 리밸런스가 발생하더라도 중복 메시지 소비를 최소화하면서 안정적으로 Kafka 메시지를 소비하는 방법
 *
 * ## 카프카 리밸런스 발생 시 중복 메시지 소비 이슈
 * 오프셋 커밋이란? 파티션에서의 현재 위치를 업데이트하는 작업을 오프셋 커밋이라고 한다.
 *
 * `enable.auto.commit=true` 일 경우, Kafka가 주기적으로 오프셋을 자동 커밋한다.
 * 이때, 리밸런스가 발생하면 메시지가 중복 소비될 수 있다.
 * - 리밸런스란?
 *   - Kafka 컨슈머 그룹에서 파티션 재분배가 일어나는 과정으로, 컨슈머가 기존 파티션을 잃고 다른 컨슈머가 맡게 된다.
 *   - 이 과정에서 아직 커밋되지 않은 메시지들이 다시 소비될 가능성이 있다.
 *
 * ## 시나리오
 * 1. consumer.poll()로 큰 메시지 배치를 리턴받고 처리 중일 때 리밸런스가 발생한다.
 * 2. 마지막 처리된 오프셋이 커밋되지 않은 상태에서 같은 메시지가 중복 소비된다.
 *
 * ## 해결 방법
 * 1. 배치 처리 중에도 비동기 커밋을 사용하여 최신 오프셋을 저장함으로써, 중복 메시지 소비를 방지한다.
 * 2. 비동기 커밋(commitAsync())는 성능이 빠르며, 커밋 실패 시 콜백 함수에서 예외를 로깅하여 영향을 최소화한다.
 *
 * ## 오프셋 커밋 시 주의사항
 * - `commitSync()`와 `commitAsync()`는 **아직 처리되지 않은 메시지의 마지막 오프셋**을 커밋한다.
 * - 리밸런스가 발생하거나 큰 메시지 배치가 처리 중일 때, 커밋해야 할 오프셋을 명시적으로 지정해야 한다.
 * - 이를 위해서는 단순히 `commitSync()`나 `commitAsync()`만 호출하는 것이 아니라, 커밋할 정확한 오프셋을 지정해야 한다.
 * </pre>
 */
@Disabled
@Slf4j
class ManualOffsetCommitTest {

	@DisplayName("컨슈머 종료 직전에 동기적 커밋과 비동기적 커밋 함께 사용하여 마지막 오프셋 커밋")
	@Test
	void test() {
		try (Consumer<String, String> consumer = new KafkaConsumer<>(getProperties())) {
			// 오프셋을 추적하기 위한 맵 (수동 커밋을 위해 사용)
			Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
			Duration timeout = Duration.ofMillis(100);
			int count = 0;
			consumer.subscribe(List.of(Constants.TopicName.SAMPLE));
			consumer.subscribe(List.of(Constants.TopicName.SAMPLE)); // Kafka에서 구독할 토픽 지정

			while (true) {
				// Kafka 브로커에서 메시지를 가져옴
				ConsumerRecords<String, String> records = consumer.poll(timeout);

				for (ConsumerRecord<String, String> record : records) {
					// 로그 출력 (메시지 정보 기록)
					log.info("Kafka Message Consumed - Topic: {}, Partition: {}, Offset: {}, Key: {}, Value: {}, Timestamp: {}",
						 record.topic(), record.partition(), record.offset(), record.key(), record.value(), record.timestamp());

					// 다음에 읽을 오프셋을 저장 (record.offset() + 1)
					currentOffsets.put(
						 new TopicPartition(record.topic(), record.partition()),
						 new OffsetAndMetadata(record.offset() + 1, "tx-id-001")
					);

					// 10개 단위로 비동기 오프셋 커밋
					// 운영 환경에서는 시간 혹은 레코드의 내용을 기준으로 커밋
					if (count % 10 == 0) {
						// commit offset.
						consumer.commitAsync(
							 currentOffsets,
							 (offsets, exception) -> {
								 if (exception != null) {
									 log.error("Async Commit Failed for Offsets: {} - Exception: {}", offsets, exception.getMessage());
								 }
							 });
					}
					count++;
				}
			}
		} catch (Exception e) {
			log.error("Kafka Consumer Error: {}", e.getMessage());
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
}
