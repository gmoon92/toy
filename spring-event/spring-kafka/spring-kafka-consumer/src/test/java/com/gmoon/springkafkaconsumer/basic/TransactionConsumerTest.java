package com.gmoon.springkafkaconsumer.basic;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @see <a href="https://github.com/apache/kafka/blob/41e4e93b5ae8a7d221fce1733e050cb98ac9713c/streams/src/main/java/org/apache/kafka/streams/processor/internals/StreamTask.java#L346">apache kafka - kafka streams processor</a>
 * @see <a href="https://www.baeldung.com/kafka-exactly-once">baeldung - kafak exactly once</a>
 * */
@Disabled
@Slf4j
class TransactionConsumerTest {

	@DisplayName("Topic to Topic - End-to-End 트랜잭션 처리 테스트")
	@Test
	void test() {
		// 1. 트랜잭션 프로듀서 초기화
		Producer<String, String> producer = getTransactionProducer();
		producer.initTransactions(); // 트랜잭션 초기화
		try (Consumer<String, String> consumer = new KafkaConsumer<>(getProperties())) {
			consumer.subscribe(List.of("topic.input")); // 2. 소스 토픽 구독

			while (true) {
				// 3. 메시지 폴링
				ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
				producer.beginTransaction(); // 4. 트랜잭션 시작

				// 5. 커밋할 오프셋 정보
				Map<TopicPartition, OffsetAndMetadata> offsetsToCommit = new HashMap<>();
				for (var record : records) {
					// 6. 메시지 컨슘, 아래 로깅으로 프로세스 처리 대체.
					log.info("Kafka Message Consumed - Topic: {}, Partition: {}, Offset: {}, Key: {}, Value: {}, Timestamp: {}",
						 record.topic(), record.partition(), record.offset(), record.key(), record.value(), record.timestamp());

					// 7. 파티션별 최종 오프셋 계산
					for (TopicPartition partition : records.partitions()) {
						// 각 파티션의 마지막 메시지의 오프셋을 가져옴
						List<ConsumerRecord<String, String>> partitionedRecords = records.records(partition);
						long lastOffset = partitionedRecords.get(partitionedRecords.size() - 1).offset();
						offsetsToCommit.put(
							 partition,
							 new OffsetAndMetadata(lastOffset + 1) // 다음에 읽을 오프셋 위치 저장
						);
					}

					// 8. 메시지 처리 후 결과 전송
					producer.send(new ProducerRecord<>("topic.output", "처리가 완료된 이벤트"));
				}

				// 9. 오프셋 커밋을 트랜잭션에 포함. 프로듀서가 직점함.
				producer.sendOffsetsToTransaction(offsetsToCommit, consumer.groupMetadata());
				producer.commitTransaction(); // 10. 트랜잭션 커밋
			}
		} catch (Exception e) {
			// 11. 예외 발생 시 트랜잭션 롤백
			log.error("Kafka Consumer Error: {}", e.getMessage());
			producer.abortTransaction(); // 트랜잭션 중단
		}
	}

	private Producer<String, String> getTransactionProducer() {
		Properties props = new Properties();
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

		// 멱등성 프로듀서 설정
		props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
		props.put(ProducerConfig.ACKS_CONFIG, "all");
		props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");

		// TRANSACTIONAL_ID_CONFIG
		// 동일한 transactional.id 한에서 정확히 한번을 보장한다.
		// 프로듀서 프로세스마다 고유한 아이디로 설정해야함
		props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "prod-uuid-01");
		return new KafkaProducer<>(props);
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
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); // 컨슈머 오프셋을 찾지 못한 경우 최근 메시지를 가져옴

		// tx-consumer options
		props.put("isolation.level", "read_committed"); // 커밋된 메시지만 읽음
		props.put("enable.auto.commit", "false"); // 오토 커밋 비활성화
		return props;
	}
}
