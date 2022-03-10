package com.gmoon.springdataredis.redis;

import java.io.Serializable;
import java.time.Duration;

public interface Cache extends Serializable {
	String getKey();
	Duration getTtl();
}
