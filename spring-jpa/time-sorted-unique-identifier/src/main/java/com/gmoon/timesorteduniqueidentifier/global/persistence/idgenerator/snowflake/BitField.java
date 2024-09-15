package com.gmoon.timesorteduniqueidentifier.global.persistence.idgenerator.snowflake;

interface BitField {

	BitAllocation getBitAllocation();
	long getValue();

	default long shift() {
		return getBitAllocation().shift();
	}
}
