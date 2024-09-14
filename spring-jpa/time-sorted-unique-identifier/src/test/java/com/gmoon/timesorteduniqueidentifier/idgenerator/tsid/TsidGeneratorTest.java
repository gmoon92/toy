package com.gmoon.timesorteduniqueidentifier.idgenerator.tsid;

import com.gmoon.timesorteduniqueidentifier.idgenerator.base.BitUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.RepeatedTest;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class TsidGeneratorTest {

	@RepeatedTest(100)
	void generate() {
		TsidGenerator generator = new TsidGenerator();

		long tsid = generator.generate();
		long timestamp = BitUtils.extract(tsid, TsidGenerator.Bits.RANDOMNESS, TsidGenerator.Bits.TIMESTAMP);
		long random = BitUtils.extract(tsid, 0, TsidGenerator.Bits.RANDOMNESS);
		log.info("TSID       : {}", tsid);
		log.info("timestamp  : {}", Instant.ofEpochMilli(timestamp));
		log.info("randomness : {}", random);

		assertThat(tsid).isPositive();
	}
}
