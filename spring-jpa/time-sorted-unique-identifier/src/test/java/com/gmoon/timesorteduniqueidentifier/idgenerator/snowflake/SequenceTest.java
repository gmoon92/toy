package com.gmoon.timesorteduniqueidentifier.idgenerator.snowflake;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class SequenceTest {

	@Test
	void test() {
		Timestamp timestamp = Timestamp.create();

		Sequence sequence = Sequence.create(timestamp);

		long value = sequence.getValue();
		log.info("{}", value);
	}
}
