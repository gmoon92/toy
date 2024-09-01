package com.gmoon.timesorteduniqueidentifier.idgenerator.snowflake;

interface BitField {

	BitAllocation getBitAllocation();

	default long getBitMask() {
		BitAllocation allocation = getBitAllocation();
		return allocation.getBitMask();
	}

	default long shiftLeft() {
		return getBitAllocation().shiftLeft();
	}

	default long shiftRight() {
		return getBitAllocation().shiftRight();
	}
}
