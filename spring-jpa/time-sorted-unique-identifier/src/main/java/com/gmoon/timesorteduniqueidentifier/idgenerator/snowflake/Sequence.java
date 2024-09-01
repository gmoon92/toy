package com.gmoon.timesorteduniqueidentifier.idgenerator.snowflake;

import lombok.Getter;

@Getter
class Sequence implements BitField {

	private BitAllocation bitAllocation = BitAllocation.SEQUENCE;
	private long value = 0L;

	private Sequence(Timestamp timestamp) {
		increment(timestamp);
	}

	static Sequence create(Timestamp timestamp) {
		return new Sequence(timestamp);
	}

	public void increment(Timestamp timestamp) {
		if (timestamp.hasTimestampCollision()) {
			// 동일한 타임스탬프에서 시퀀스를 증가
			long sequenceMask = getBitMask();
			value = (value + 1) & sequenceMask;
		} else {
			value = 0L;
		}
	}

	public boolean isZero() {
		return value == 0L;
	}
}
