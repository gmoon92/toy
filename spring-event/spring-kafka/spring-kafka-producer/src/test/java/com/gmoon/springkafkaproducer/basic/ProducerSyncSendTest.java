package com.gmoon.springkafkaproducer.basic;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.StringSerializer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Disabled
@DisplayName("동기 메시지 전송 방식")
class ProducerSyncSendTest {

	@DisplayName("메시지 동기 전송")
	@Test
	void syncSend() {
		try (Producer<String, String> producer = new KafkaProducer<>(getProperties())) {
			var topic = "topic.sample";
			var partition = 0;
			var key = "1";
			var value = "hello";
			var timestamp = System.currentTimeMillis();
			Iterable<Header> headers = List.of(
				 new RecordHeader("correlation-id", toBytes("123e4567-e89b-12d3-a456-426614174000")),
				 new RecordHeader("content-type", toBytes("application/json")),
				 new RecordHeader("X-Auth-Token", toBytes("eyJhbGciOiJI....")),
				 new RecordHeader("X-Trace-Id", toBytes("trace-123456")),
				 new RecordHeader("X-Retry-Count", toBytes("1"))
			);

			ProducerRecord<String, String> record = new ProducerRecord<>(
				 topic,         // [필수] 메시지를 전송할 카프카 토픽
				 partition,     // [선택] 특정 파티션을 지정하여 메시지 전송 가능
				 timestamp,     // [선택] 레코드의 타임스템프(epoch ms)
				 key,           // [선택] 같은 키 값을 가진 메시지는 동일한 파티션으로 전송됨 (파티셔너가 결정)
				                //       토픽에 파티션을 추가하는 순간 키와 파티션의 일관성은 보장되지 않음. 주의 필요.
				 value,         // [필수] 실제 메시지 값
				 headers        // [선택] 레코드에 포함될 헤더
			);

			Future<RecordMetadata> send = producer.send(record);
			RecordMetadata metadata = send.get(); // 동기 메시지 전송 처리
			log.info("message               : {}", metadata);
			log.info("serialized key size   : {}", metadata.serializedKeySize());
			log.info("serialized value size : {}", metadata.serializedValueSize());
			log.info("topic                 : {}", metadata.topic());
			log.info("partition             : {}", metadata.partition());
			log.info("offset                : {}", metadata.offset());
			log.info("timestamp             : {}", metadata.timestamp());

			Assertions.assertThat(metadata.topic()).isEqualTo("topic.sample");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private byte[] toBytes(String value) {
		return value.getBytes(StandardCharsets.UTF_8);
	}

	/**
	 * @apiNote <a href="https://kafka.apache.org/documentation/#producerconfigs">프로듀서 옵션</a>
	 * <pre>
	 *     주요 프로듀서 옵션
	 *     - <a href="https://kafka.apache.org/documentation/#producerconfigs_bootstrap.servers">bootstrap.servers</a>
	 *     - <a href="https://kafka.apache.org/documentation/#producerconfigs_client.dns.lookup">client.dns.lookup</a>
	 *     - <a href="https://kafka.apache.org/documentation/#producerconfigs_acks">acks</a>
	 *     - <a href="https://kafka.apache.org/documentation/#producerconfigs_buffer.memory">buffer.memory</a>
	 *     - <a href="https://kafka.apache.org/documentation/#producerconfigs_compression.type">compression.type</a>
	 *     - <a href="https://kafka.apache.org/documentation/#producerconfigs_enable.idempotence">enable.idempotence</a>
	 *     - <a href="https://kafka.apache.org/documentation/#producerconfigs_max.in.flight.requests.per.connection">max.in.flight.requests.per.connection</a>
	 *     - <a href="https://kafka.apache.org/documentation/#producerconfigs_retries">retries</a>
	 *     - <a href="https://kafka.apache.org/documentation/#producerconfigs_batch.size">batch.size</a>
	 *     - <a href="https://kafka.apache.org/documentation/#producerconfigs_linger.ms">linger.ms</a>
	 *     - <a href="https://kafka.apache.org/documentation/#producerconfigs_transactional.id">transactional.id</a>
	 * </pre>
	 */
	private Properties getProperties() {
		Properties props = new Properties();
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		return props;
	}
}



