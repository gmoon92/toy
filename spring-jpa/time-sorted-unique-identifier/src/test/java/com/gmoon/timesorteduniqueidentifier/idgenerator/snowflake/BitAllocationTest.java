package com.gmoon.timesorteduniqueidentifier.idgenerator.snowflake;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static com.gmoon.timesorteduniqueidentifier.idgenerator.snowflake.BitAllocation.*;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class BitAllocationTest {

	@Test
	void shift() {
		assertThat(SIGN_BIT.shift()).isEqualTo(TIMESTAMP.bits + DATA_CENTER.bits + WORKER_ID.bits + SEQUENCE.bits);
		assertThat(TIMESTAMP.shift()).isEqualTo(DATA_CENTER.bits + WORKER_ID.bits + SEQUENCE.bits);
		assertThat(DATA_CENTER.shift()).isEqualTo(WORKER_ID.bits + SEQUENCE.bits);
		assertThat(WORKER_ID.shift()).isEqualTo(SEQUENCE.bits);
		assertThat(SEQUENCE.shift()).isZero();
	}
}
