package com.gmoon.resourceserver.cors;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Origin implements Serializable {
	@Column
	private String schema;

	@Column
	private String host;

	@Column
	private Integer port;

	@Builder
	private Origin(String schema, String host, Integer port) {
		this.schema = schema;
		this.host = host;
		this.port = port;
	}
}
