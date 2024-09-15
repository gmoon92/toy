package com.gmoon.timesorteduniqueidentifier.global.persistence.idgenerator.snowflake;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SequenceTest {

	@Test
	void test() {
		Timestamp timestamp = Timestamp.create();

		Sequence sequence = Sequence.create(timestamp);

		assertThat(sequence.getValue()).isZero();
	}

	@DisplayName("동시성 이슈가 발생되면 시퀀스 증가")
	@Test
	void incrementOrReset() {
		Timestamp timestamp = Timestamp.create();
		Sequence sequence = Sequence.create(timestamp);

		timestamp.resolveSequenceCollision(sequence);
		sequence.incrementOrReset(timestamp);
		sequence.incrementOrReset(timestamp);

		assertThat(sequence.getValue()).isEqualTo(2);
	}
}
