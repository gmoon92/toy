package com.gmoon.resourceserver.cors;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Embeddable
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Origin implements Serializable {
	@Column(name = "schema")
	private String schema;

	@Column(name = "host")
	private String host;

	@Column(name = "port")
	private Integer port;

	@Builder
	private Origin(String schema, String host, Integer port) {
		this.schema = schema;
		this.host = host;
		this.port = port;
	}
}
