package com.gmoon.springquartzcluster.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@ConfigurationProperties(prefix = "service-check")
public class QuartzProperties {
	private final JvmMemoryUsage jvmMemoryUsage;
	private final DiskUsage diskSpace;
	private final CpuUsage cpuUsage;

	@ConstructorBinding
	public QuartzProperties(JvmMemoryUsage jvmMemoryUsage, DiskUsage diskSpace, CpuUsage cpuUsage) {
		this.jvmMemoryUsage = jvmMemoryUsage;
		this.diskSpace = diskSpace;
		this.cpuUsage = cpuUsage;
	}

	@RequiredArgsConstructor
	private static class CpuUsage {
		private final boolean enabled;
		private final int thresholdPercentage;
	}

	@RequiredArgsConstructor
	private static class JvmMemoryUsage {
		private final boolean enabled;
		private final int thresholdPercentage;
	}

	@RequiredArgsConstructor
	private static class DiskUsage {
		private final boolean enabled;
		private final int thresholdPercentage;
	}

	public boolean isCpuUsageEnabled() {
		return cpuUsage.enabled;
	}

	public int getCpuThresholdPercentage() {
		return cpuUsage.thresholdPercentage;
	}

	public boolean isJvmMemoryUsageEnabled() {
		return jvmMemoryUsage.enabled;
	}

	public int getJvmMemoryThresholdPercentage() {
		return jvmMemoryUsage.thresholdPercentage;
	}

	public boolean isDiskUsageEnabled() {
		return diskSpace.enabled;
	}

	public int getDiskThresholdPercentage() {
		return diskSpace.thresholdPercentage;
	}
}
