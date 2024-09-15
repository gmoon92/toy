package com.gmoon.timesorteduniqueidentifier.global.persistence.idgenerator.snowflake;

class WorkerId implements BitField {

    private final BitAllocation bitAllocation = BitAllocation.WORKER_ID;
    private final long value;

    WorkerId(long workerId) {
        final long maxBits = getBitAllocation().bitMask;
        if (workerId > maxBits || workerId < 0) {
            throw new IllegalArgumentException("worker Id(" + workerId + ") can't be greater than " + maxBits + " or less than 0");
        }

        this.value = workerId;
    }

    static WorkerId create(long workerId) {
        return new WorkerId(workerId);
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
