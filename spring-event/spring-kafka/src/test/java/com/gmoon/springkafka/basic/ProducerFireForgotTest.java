package com.gmoon.springkafka.basic;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;

@Slf4j
class ProducerFireForgotTest {

	@Test
	void name() {
		log.info("{}", StringSerializer.class.getName());
	}

}
