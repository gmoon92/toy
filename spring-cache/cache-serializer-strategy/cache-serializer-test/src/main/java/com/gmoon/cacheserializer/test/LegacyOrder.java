package com.gmoon.cacheserializer.test;

import java.io.Serial;
import java.io.Serializable;

public class LegacyOrder implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private final long id;
	private final String status;

	public LegacyOrder(long id, String status) {
		this.id = id;
		this.status = status;
	}

	public long getId() {
		return id;
	}

	public String getStatus() {
		return status;
	}
}
