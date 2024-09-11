package com.gmoon.timesorteduniqueidentifier.idgenerator.snowflake;

import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

/**
 * <pre>
 * {sign bit}{timestamp}{instance}{sequence}
 *    1bits     41bits    10bits    12bits
 * </pre>
 */
@Slf4j
public class SnowflakeIdGenerator {

	private final Timestamp timestamp;
	private final WorkerId workerId;
	private final DataCenterId dataCenterId;
	private final Sequence sequence;

	public SnowflakeIdGenerator(long workerId, long dataCenterId) {
		this.timestamp = Timestamp.create();
		this.workerId = WorkerId.create(workerId);
		this.dataCenterId = DataCenterId.create(dataCenterId);
		this.sequence = Sequence.create(timestamp);

		log.info("worker starting. workerId: {}, dataCenterId: {}", workerId, dataCenterId);
	}

	@Synchronized
	public long generate() {
		timestamp.reset();
		sequence.incrementOrReset(timestamp);
		timestamp.resolveSequenceCollision(sequence);

		return nextId(timestamp, sequence);
	}

	private long nextId(Timestamp timestamp, Sequence sequence) {
		return (timestamp.getValue() << timestamp.shift())
			 | (dataCenterId.getValue() << dataCenterId.shift())
			 | (workerId.getValue() << workerId.shift())
			 | sequence.getValue();
	}
}
