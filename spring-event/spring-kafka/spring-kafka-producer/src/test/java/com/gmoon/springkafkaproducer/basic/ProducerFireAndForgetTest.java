package com.gmoon.springkafkaproducer.basic;

import java.util.Properties;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Disabled
@DisplayName("Fire-and-Forget 메시지 전송 방식")
class ProducerFireAndForgetTest {

	@DisplayName("최대 한번(At Most Once) 전송 방식")
	@Test
	void send() {
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // 카프카 브로커 서버 설정
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName()); // 시리얼라이저 설정
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "false"); // 멱등성 프로듀서 비활성화
		props.put(ProducerConfig.ACKS_CONFIG, "0"); // 0 ack 응답 확인 없음

		try (Producer<String, String> producer = new KafkaProducer<>(props)) {
			// 레코드 생성
			ProducerRecord<String, String> record = new ProducerRecord<>("topic.sample", "hello");

			// 메시지 전송
			Future<RecordMetadata> send = producer.send(record);

			// 메타 데이터 확인
			RecordMetadata metadata = send.get();
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
}
