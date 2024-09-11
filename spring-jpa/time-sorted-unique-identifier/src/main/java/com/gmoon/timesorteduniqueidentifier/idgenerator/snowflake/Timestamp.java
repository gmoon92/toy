package com.gmoon.timesorteduniqueidentifier.idgenerator.snowflake;

import com.gmoon.timesorteduniqueidentifier.idgenerator.exception.InvalidSystemClockException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class Timestamp implements BitField {

	private static final long TWITTER_EPOCH = LocalDateTime.of(2010, 11, 4, 1, 42, 54, 657000000)
		 .toInstant(ZoneOffset.UTC)
		 .toEpochMilli();

	private final BitAllocation bitAllocation = BitAllocation.TIMESTAMP;
	private long lastGeneratedTimestamp = -1L;
	private long value;

	public static Timestamp create() {
		Timestamp timestamp = new Timestamp();
		return timestamp.reset();
	}

	private void checkClockMovingBackwards(long timestamp) {
		if (timestamp < -1) {
			log.error("Clock is moving backwards. Rejecting requests until {}", -1);
			throw new InvalidSystemClockException("Clock moved backwards. Refusing to generate id for " + (lastGeneratedTimestamp - timestamp) + " milliseconds");
		}
	}

	public Timestamp reset() {
		this.value = System.currentTimeMillis();
		checkClockMovingBackwards(value);
		return this;
	}

	public void resolveSequenceCollision(Sequence sequence) {
		boolean duplicated = sequence.isZero() && hasConflict();
		if (duplicated) {
			// 시퀀스가 최대치에 도달하면, 다음 밀리초로 이동
			lastGeneratedTimestamp = waitForNextMillis();
		} else {
			lastGeneratedTimestamp = value;
		}
	}

	private long waitForNextMillis() {
		while (value <= lastGeneratedTimestamp) {
			value = System.currentTimeMillis();
		}
		return value;
	}

	public boolean hasConflict() {
		return lastGeneratedTimestamp == value;
	}

	public static Instant toInstant(long timestamp) {
		return Instant.ofEpochMilli(timestamp + Timestamp.TWITTER_EPOCH);
	}

	@Override
	public long getValue() {
		long timestamp = value - TWITTER_EPOCH;
		return bitAllocation.masking(timestamp);
	}

	@Override
	public BitAllocation getBitAllocation() {
		return bitAllocation;
	}
}
