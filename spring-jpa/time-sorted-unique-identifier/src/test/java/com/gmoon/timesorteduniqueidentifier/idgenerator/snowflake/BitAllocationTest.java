package com.gmoon.timesorteduniqueidentifier.idgenerator.snowflake;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static com.gmoon.timesorteduniqueidentifier.idgenerator.snowflake.BitAllocation.*;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class BitAllocationTest {

	@Test
	void shift() {
		assertThat(SIGN_BIT.shift()).isEqualTo(TIMESTAMP.bitLength + DATA_CENTER.bitLength + WORKER_ID.bitLength + SEQUENCE.bitLength);
		assertThat(TIMESTAMP.shift()).isEqualTo(DATA_CENTER.bitLength + WORKER_ID.bitLength + SEQUENCE.bitLength);
		assertThat(DATA_CENTER.shift()).isEqualTo(WORKER_ID.bitLength + SEQUENCE.bitLength);
		assertThat(WORKER_ID.shift()).isEqualTo(SEQUENCE.bitLength);
		assertThat(SEQUENCE.shift()).isZero();
	}
}
