package com.gmoon.springkafkaproducer.basic;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Properties;

@Slf4j
@Disabled
@DisplayName("정확히 한 번")
class ProducerExactlyOnceSendTest {

	/**
	 * @see <a href="https://kafka.apache.org/documentation/#usingtransactions">Apache Kafka - Using Transactions</a>
	 */
	@Test
	void withTransaction() {
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
		props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "gmoon-uuid-01");

		Producer<String, String> producer = new KafkaProducer<>(props);
		try {
			producer.initTransactions(); // 트랜잭션 초기화
			producer.beginTransaction(); // 트랜잭션 시작

			var record = new ProducerRecord<String, String>("topic.sample", "callback");
			RecordMetadata recordMetadata = producer.send(record).get();
			log.info(recordMetadata.toString());

			producer.flush();
			producer.commitTransaction(); // 트랜잭션 커밋
		} catch (Exception e) {
			producer.abortTransaction(); // 트랜잭션 중단
			throw new RuntimeException(e);
		} finally {
			producer.close();
		}
	}

}
