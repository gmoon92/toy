package com.gmoon.timesorteduniqueidentifier.idgenerator.snowflake;

import lombok.Getter;

@Getter
class Worker implements BitField {
	private final BitAllocation bitAllocation = BitAllocation.WORKER_ID;
	private final long id;

	Worker(long workerId) {
		final long maxBits = getBitMask();
		if (workerId > maxBits || workerId < 0) {
			throw new IllegalArgumentException("worker Id(" + workerId + ") can't be greater than " + maxBits + " or less than 0");
		}

		this.id = workerId;
	}

	static Worker create(long workerId) {
		return new Worker(workerId);
	}
}
