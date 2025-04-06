package com.gmoon.springkafkaconsumer.basic;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

@Disabled
@Slf4j
class ConsumerTest {

	private final String topic = "sample";

	@Test
	void sync() {
		try (Consumer<String, String> consumer = new KafkaConsumer<>(getProperties())) {
			// Kafka에서 구독할 토픽 지정
			consumer.subscribe(List.of(topic));

			while (true) {
				// Kafka 브로커에서 메시지를 가져옴
				ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
				for (ConsumerRecord<String, String> record : records) {
					log.info("Kafka Message Consumed - Topic: {}, Partition: {}, Offset: {}, Key: {}, Value: {}, Timestamp: {}",
						 record.topic(), record.partition(), record.offset(), record.key(), record.value(), record.timestamp());

					// commit offset.
					consumer.commitSync();
				}
			}
		} catch (Exception e) {
			log.error("Kafka Consumer Error: {}", e.getMessage());
		}
	}

	@Test
	void asyncCallback() {
		try (Consumer<String, String> consumer = new KafkaConsumer<>(getProperties())) {
			consumer.subscribe(List.of(topic));

			while (true) {
				ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

				for (ConsumerRecord<String, String> record : records) {
					log.info("Kafka Message Consumed - Topic: {}, Partition: {}, Offset: {}, Key: {}, Value: {}, Timestamp: {}",
						 record.topic(), record.partition(), record.offset(), record.key(), record.value(), record.timestamp());

					consumer.commitAsync(
						 // callback.
						 (offsets, exception) -> {
							 if (exception != null) {
								 log.error("Async Commit Failed for Offsets: {} - Exception: {}", offsets, exception.getMessage());
							 }
						 });
				}
			}
		} catch (Exception e) {
			log.error("Kafka Consumer Error: {}", e.getMessage());
		}
	}

	/**
	 * @apiNote <a href="https://kafka.apache.org/documentation/#consumerconfigs">컨슈머 옵션</a>
	 * <pre>
	 *     주요 컨슈머 옵션
	 *     - <a href="https://kafka.apache.org/documentation/#consumerconfigs_key.deserializer">key.deserializer</a>
	 *     - <a href="https://kafka.apache.org/documentation/#consumerconfigs_value.deserializer">value.deserializer</a>
	 *     - <a href="https://kafka.apache.org/documentation/#consumerconfigs_bootstrap.servers">bootstrap.servers</a>
	 *     - <a href="https://kafka.apache.org/documentation/#consumerconfigs_fetch.min.bytes">fetch.min.bytes</a>
	 *     - <a href="https://kafka.apache.org/documentation/#consumerconfigs_group.id">group.id</a>
	 *     - <a href="https://kafka.apache.org/documentation/#consumerconfigs_heartbeat.interval.ms">heartbeat.interval.ms</a>
	 *     - <a href="https://kafka.apache.org/documentation/#consumerconfigs_max.partition.fetch.bytes">max.partition.fetch.bytes</a>
	 *     - <a href="https://kafka.apache.org/documentation/#consumerconfigs_session.timeout.ms">session.timeout.ms</a>
	 *     - <a href="https://kafka.apache.org/documentation/#consumerconfigs_enable.auto.commit">enable.auto.commit</a>
	 *     - <a href="https://kafka.apache.org/documentation/#consumerconfigs_auto.offset.reset">auto.offset.reset</a>
	 *     - <a href="https://kafka.apache.org/documentation/#consumerconfigs_fetch.max.bytes">fetch.max.bytes</a>
	 *     - <a href="https://kafka.apache.org/documentation/#consumerconfigs_group.instance.id">group.instance.id</a>
	 *     - <a href="https://kafka.apache.org/documentation/#consumerconfigs_isolation.level">isolation.level</a>
	 *     - <a href="https://kafka.apache.org/documentation/#consumerconfigs_max.poll.records">max.poll.records</a>
	 *     - <a href="https://kafka.apache.org/documentation/#consumerconfigs_partition.assignment.strategy">partition.assignment.strategy</a>
	 *     - <a href="https://kafka.apache.org/documentation/#consumerconfigs_fetch.max.wait.ms">fetch.max.wait.ms</a>
	 * </pre>
	 */
	private Properties getProperties() {
		Properties props = new Properties();
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // 브로커 서버 지정
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-group01"); // 컨슈머 그룹 지정
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true"); // 오토 커밋 활성화
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); // 컨슈머 오프셋을 찾지 못한 경우 최근 메시지를 가져옴
		props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed"); // 커밋된 메시지만 읽음
		return props;
	}
}
