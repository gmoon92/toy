package com.gmoon.timesorteduniqueidentifier.idgenerator.snowflake;

import com.gmoon.timesorteduniqueidentifier.idgenerator.exception.InvalidSystemClock;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
class Timestamp implements BitField {
	private static final long TWITTER_EPOCH = LocalDateTime.of(2010, 11, 4, 1, 42, 54, 657000000)
		 .toInstant(ZoneOffset.UTC)
		 .toEpochMilli();

	@Getter
	private final BitAllocation bitAllocation = BitAllocation.TIMESTAMP;
	private long lastGeneratedTimestamp = -1L;
	private long value;

	public static Timestamp create() {
		Timestamp timestamp = new Timestamp();
		return timestamp.initialize();
	}

	private void checkClockMovingBackwards(long timestamp) {
		if (timestamp < -1) {
			log.error("Clock is moving backwards. Rejecting requests until {}", -1);
			throw new InvalidSystemClock("Clock moved backwards. Refusing to generate id for " + (lastGeneratedTimestamp - timestamp) + " milliseconds");
		}
	}

	public Timestamp initialize() {
		this.value = System.currentTimeMillis();
		checkClockMovingBackwards(value);
		return this;
	}

	public long getValue() {
		return value - TWITTER_EPOCH;
	}

	public void update(Sequence sequence) {
		boolean duplicated = sequence.isZero() && hasTimestampCollision();
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

	public boolean hasTimestampCollision() {
		return lastGeneratedTimestamp == value;
	}

	public static Instant toInstant(long timestamp) {
		return Instant.ofEpochMilli(timestamp + Timestamp.TWITTER_EPOCH);
	}
}
