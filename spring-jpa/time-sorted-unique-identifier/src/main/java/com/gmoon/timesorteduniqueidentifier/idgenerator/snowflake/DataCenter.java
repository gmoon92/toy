package com.gmoon.timesorteduniqueidentifier.idgenerator.snowflake;

import lombok.Getter;

@Getter
class DataCenter implements BitField {
	private final BitAllocation bitAllocation = BitAllocation.DATA_CENTER;
	private final long id;

	DataCenter(long datacenterId) {
		final long maxBits = getBitMask();
		if (datacenterId > maxBits || datacenterId < 0) {
			throw new IllegalArgumentException("datacenter Id(" + datacenterId + ") can't be greater than " + maxBits + " or less than 0");
		}
		this.id = datacenterId;
	}

	static DataCenter create(long dataCenterId) {
		return new DataCenter(dataCenterId);
	}
}
