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
	private final InstanceId instanceId;
	private final Sequence sequence;

	public SnowflakeIdGenerator(long workerId, long dataCenterId) {
		this.timestamp = Timestamp.create();
		this.instanceId = InstanceId.create(workerId, dataCenterId);
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
		long workerId = instanceId.getWorkerId();
		long dataCenterId = instanceId.getDataCenterId();
		long workerIdBits = instanceId.getWorkerIdBits();
		long dataCenterIdBits = instanceId.getDataCenterIdBits();

		final long sequenceBits = sequence.getLength();
		final long workerIdLeftShift = sequenceBits;
		final long datacenterIdLeftShift = sequenceBits + workerIdBits;
		final long timestampLeftShift = sequenceBits + workerIdBits + dataCenterIdBits;

		return (timestamp.getValue() << timestampLeftShift)
			 | (dataCenterId << datacenterIdLeftShift)
			 | (workerId << workerIdLeftShift)
			 | sequence.getValue();
	}
}
