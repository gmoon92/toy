package com.gmoon.springkafkaconsumer.mails;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Disabled
class UserNotificationServiceTest {

	@Test
	void processEvent() {
		try (Consumer<String, String> consumer = new KafkaConsumer<>(getProperties())) {
			// Kafka에서 구독할 토픽 지정
			consumer.subscribe(Pattern.compile("outbox\\.event\\..*"));

			while (true) {
				// Kafka 브로커에서 메시지를 가져옴
				ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10));
				for (ConsumerRecord<String, String> record : records) {
					log.info("Consumed Message -> Topic: {}, Partition: {}, Offset: {}, Timestamp: {}, Key: {}, Value: {}, Headers: [{}]",
						 record.topic(), record.partition(), record.offset(), record.timestamp(),
						 record.key(), record.value(), formatHeaders(record.headers()));

					// commit offset.
					consumer.commitSync();
				}
			}
		} catch (Exception e) {
			log.error("Kafka Consumer Error: {}", e.getMessage());
		}
	}

	private String formatHeaders(Headers headers) {
		return StreamSupport.stream(headers.spliterator(), false)
			 .map(header -> header.key() + "=" + new String(header.value()))
			 .collect(Collectors.joining(", "));
	}

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
