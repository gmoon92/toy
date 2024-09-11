package com.gmoon.timesorteduniqueidentifier.idgenerator.snowflake;

class DataCenterId implements BitField {

    private final BitAllocation bitAllocation = BitAllocation.DATA_CENTER;
    private final long value;

    DataCenterId(long datacenterId) {
        final long maxBits = getBitAllocation().bitMask;
        if (datacenterId > maxBits || datacenterId < 0) {
            throw new IllegalArgumentException("datacenter Id(" + datacenterId + ") can't be greater than " + maxBits + " or less than 0");
        }
        this.value = datacenterId;
    }

    static DataCenterId create(long dataCenterId) {
        return new DataCenterId(dataCenterId);
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
