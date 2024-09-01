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
	private final Worker worker;
	private final DataCenter dataCenter;
	private final Sequence sequence;

	public SnowflakeIdGenerator(long workerId, long dataCenterId) {
		this.timestamp = Timestamp.create();
		this.worker = Worker.create(workerId);
		this.dataCenter = DataCenter.create(dataCenterId);
		this.sequence = Sequence.create(timestamp);

		log.info("worker starting. workerId: {}, dataCenterId: {}", workerId, dataCenterId);
	}

	@Synchronized
	public long generate() {
		timestamp.initialize();
		sequence.increment(timestamp);
		timestamp.update(sequence);

		return nextId(timestamp, sequence);
	}

	private long nextId(Timestamp timestamp, Sequence sequence) {
		final long workerIdLeftShift = worker.shiftLeft();
		final long datacenterIdLeftShift = dataCenter.shiftLeft();
		final long timestampLeftShift = timestamp.shiftLeft();

		return (timestamp.getValue() << timestampLeftShift)
			 | (dataCenter.getId() << datacenterIdLeftShift)
			 | (worker.getId() << workerIdLeftShift)
			 | sequence.getValue();
	}
}
