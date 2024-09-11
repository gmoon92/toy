package com.gmoon.timesorteduniqueidentifier.idgenerator.snowflake;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class Sequence implements BitField {

	private final BitAllocation bitAllocation = BitAllocation.SEQUENCE;
	private long value = 0L;

	static Sequence create(Timestamp timestamp) {
		Sequence sequence = new Sequence();
		sequence.incrementOrReset(timestamp);
		return sequence;
	}

	public void incrementOrReset(Timestamp timestamp) {
		if (timestamp.hasConflict()) {
			// 동시성 이슈가 발생시 시퀀스 증가
			value = value + 1;
		} else {
			value = 0L;
		}
	}

	public boolean isZero() {
		return value == 0L;
	}

	@Override
	public long getValue() {
		return bitAllocation.masking(value);
	}

	@Override
	public BitAllocation getBitAllocation() {
		return bitAllocation;
	}
}
