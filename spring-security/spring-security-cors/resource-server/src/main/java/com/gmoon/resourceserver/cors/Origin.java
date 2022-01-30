package com.gmoon.resourceserver.cors;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AccessLevel;
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
}
