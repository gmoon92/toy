package com.gmoon.springdataredis.cache;

import java.io.Serializable;
import java.time.Duration;

public interface Cache extends Serializable {
	String getKey();

	Duration getTtl();
}
