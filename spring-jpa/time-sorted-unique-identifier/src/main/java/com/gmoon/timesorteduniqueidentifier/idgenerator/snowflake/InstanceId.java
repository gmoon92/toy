package com.gmoon.timesorteduniqueidentifier.idgenerator.snowflake;

import lombok.Getter;

class InstanceId implements BitField {

	private final WorkerId worker;
	private final DataCenterId dataCenter;

	InstanceId(long workerId, long dataCenterId) {
		this.worker = new WorkerId(workerId);
		this.dataCenter = new DataCenterId(dataCenterId);
	}

	public static InstanceId create(long workerId, long dataCenterId) {
		return new InstanceId(workerId, dataCenterId);
	}

	public long getWorkerId() {
		return worker.getValue();
	}

	public long getWorkerIdBits() {
		return worker.getLength();
	}

	public long getDataCenterId() {
		return dataCenter.getValue();
	}

	public long getDataCenterIdBits() {
		return dataCenter.getLength();
	}

	@Override
	public long getLength() {
		return worker.getLength() + dataCenter.getLength();
	}

	@Getter
	static class WorkerId implements BitField {
		private final long length = 5;
		private final long value;

		WorkerId(long workerId) {
			final long maxBits = getBitMask();
			if (workerId > maxBits || workerId < 0) {
				throw new IllegalArgumentException("worker Id can't be greater than " + maxBits + " or less than 0");
			}

			this.value = workerId;
		}
	}

	@Getter
	static class DataCenterId implements BitField {
		private final long length = 5;
		private final long value;

		DataCenterId(long datacenterId) {
			final long maxBits = getBitMask();
			if (datacenterId > maxBits || datacenterId < 0) {
				throw new IllegalArgumentException("datacenter Id can't be greater than " + maxBits + " or less than 0");
			}
			this.value = datacenterId;
		}
	}
}
