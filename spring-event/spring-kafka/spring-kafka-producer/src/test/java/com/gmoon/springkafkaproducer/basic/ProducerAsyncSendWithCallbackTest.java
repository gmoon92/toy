package com.gmoon.springkafkaproducer.basic;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Properties;

@Slf4j
@Disabled
@DisplayName("비동기 콜백 메시지 전송 방식")
class ProducerAsyncSendWithCallbackTest {

	@DisplayName("메시지 비동기 전송 및 콜백 처리")
	@Test
	void test() {
		try (Producer<String, String> producer = new KafkaProducer<>(getProperties())) {
			var record = new ProducerRecord<String, String>("topic.sample", "callback");

			Assertions.assertThatCode(() ->
				 producer.send(
					  record,
					  new ProducerRecordCallback<>(record)
				 )
			).doesNotThrowAnyException();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	private record ProducerRecordCallback<K, V>(ProducerRecord<K, V> record)
		 implements Callback {

		@Override
		public void onCompletion(RecordMetadata metadata, Exception e) {
			if (e == null) {
				handleSuccess(metadata);
			} else {
				handleFailure(e);
			}
		}

		private void handleSuccess(RecordMetadata metadata) {
			log.info("Topic: {}, Partition: {}, Offset: {}, Key: {}, Received Message: {}",
				 metadata.topic(),
				 metadata.partition(),
				 metadata.offset(),
				 record.key(),
				 record.value()
			);
		}

		/**
		 * @apiNote 프로듀서 메시지 전송 실패시 예외 처리
		 */
		private void handleFailure(Exception e) {
			log.error("Failed to send message. Topic: {}, Partition: {}, Key: {}, Value: {}, Error: {}",
				 record.topic(),
				 record.partition(),
				 record.key(),
				 record.value(),
				 e.getMessage(), e
			);
		}
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
