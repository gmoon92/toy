package com.gmoon.timesorteduniqueidentifier.idgenerator.snowflake;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.gmoon.timesorteduniqueidentifier.idgenerator.snowflake.BitAllocation.*;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class BitAllocationTest {

	@Test
	void shiftLeft() {
		assertThat(SIGN_BIT.shiftLeft()).isEqualTo(TIMESTAMP.bitLength + DATA_CENTER.bitLength + WORKER_ID.bitLength + SEQUENCE.bitLength);
		assertThat(TIMESTAMP.shiftLeft()).isEqualTo(DATA_CENTER.bitLength + WORKER_ID.bitLength + SEQUENCE.bitLength);
		assertThat(DATA_CENTER.shiftLeft()).isEqualTo(WORKER_ID.bitLength + SEQUENCE.bitLength);
		assertThat(WORKER_ID.shiftLeft()).isEqualTo(SEQUENCE.bitLength);
		assertThat(SEQUENCE.shiftLeft()).isZero();
	}

	@Test
	void shiftRight() {
		assertThat(SIGN_BIT.shiftRight()).isZero();
		assertThat(TIMESTAMP.shiftRight()).isEqualTo(SIGN_BIT.bitLength);
		assertThat(DATA_CENTER.shiftRight()).isEqualTo(SIGN_BIT.bitLength + TIMESTAMP.bitLength);
		assertThat(WORKER_ID.shiftRight()).isEqualTo(SIGN_BIT.bitLength + TIMESTAMP.bitLength + DATA_CENTER.bitLength);
		assertThat(SEQUENCE.shiftRight()).isEqualTo(SIGN_BIT.bitLength + TIMESTAMP.bitLength + DATA_CENTER.bitLength + WORKER_ID.bitLength);
	}

	@Test
	void extract() {
		long snowflakeId = 1830154354842005504L;
		log.info("snowflakeId: {}", snowflakeId);

		log.info("Sign bit    : {}", BitAllocation.SIGN_BIT.extract(snowflakeId));
		log.info("Timestamp   : {}", BitAllocation.TIMESTAMP.extract(snowflakeId));
		log.info("Timestamp   : {}", Timestamp.toInstant(BitAllocation.TIMESTAMP.extract(snowflakeId)));
		log.info("Data center : {}", BitAllocation.DATA_CENTER.extract(snowflakeId));
		log.info("Worker id   : {}", BitAllocation.WORKER_ID.extract(snowflakeId));
		log.info("Sequence    : {}", BitAllocation.SEQUENCE.extract(snowflakeId));

		assertThat(Arrays.stream(values()))
			 .map(bitAllocation -> bitAllocation.extract(snowflakeId))
			 .allMatch(result -> result >= 0);
	}
}
