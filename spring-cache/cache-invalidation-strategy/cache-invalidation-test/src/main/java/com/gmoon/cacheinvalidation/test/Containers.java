package com.gmoon.cacheinvalidation.test;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

public final class Containers {

	private static final DockerImageName MYSQL_IMAGE = DockerImageName.parse("mysql:8.4.4");
	private static final DockerImageName REDIS_IMAGE = DockerImageName.parse("redis:7-alpine");
	private static final int REDIS_PORT = 6379;
	private static final boolean REUSABLE = System.getenv("CI") == null;

	private static final MySQLContainer<?> MYSQL = new MySQLContainer<>(MYSQL_IMAGE)
		 .withDatabaseName("cache_invalidation")
		 .withReuse(REUSABLE);

	private static final GenericContainer<?> REDIS = new GenericContainer<>(REDIS_IMAGE)
		 .withExposedPorts(REDIS_PORT)
		 .withReuse(REUSABLE);

	static {
		MYSQL.start();
		REDIS.start();
	}

	private Containers() {
	}

	public static MySQLContainer<?> mysql() {
		return MYSQL;
	}

	public static GenericContainer<?> redis() {
		return REDIS;
	}
}
