package com.gmoon.timesorteduniqueidentifier.idgenerator.snowflake;

interface BitField {

	long getLength();

	default long getBitMask() {
		return ~(-1L << getLength());
	}
}
