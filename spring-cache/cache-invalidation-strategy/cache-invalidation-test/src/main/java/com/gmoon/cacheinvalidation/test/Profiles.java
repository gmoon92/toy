package com.gmoon.cacheinvalidation.test;

public final class Profiles {

	public static final String TEST = "test";
	public static final String TESTCONTAINERS = "testcontainers";
	public static final String CONTAINERS_ENABLED_PROPERTY = "cache-invalidation.test.containers";

	private Profiles() {
	}

	public static boolean containersEnabled() {
		return Boolean.parseBoolean(System.getProperty(CONTAINERS_ENABLED_PROPERTY, "false"));
	}
}
