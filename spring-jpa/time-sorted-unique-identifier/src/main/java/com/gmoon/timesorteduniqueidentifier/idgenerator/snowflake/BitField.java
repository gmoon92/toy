package com.gmoon.timesorteduniqueidentifier.idgenerator.snowflake;

interface BitField {

	BitAllocation getBitAllocation();
	long getValue();

	default long shift() {
		return getBitAllocation().shift();
	}
}
