package com.gmoon.timesorteduniqueidentifier.idgenerator.snowflake;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SequenceTest {

	@Test
	void test() {
		Timestamp timestamp = Timestamp.create();

		Sequence sequence = Sequence.create(timestamp);

		assertThat(sequence.getValue()).isZero();
	}
}
