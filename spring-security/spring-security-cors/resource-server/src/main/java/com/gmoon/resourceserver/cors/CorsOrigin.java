package com.gmoon.resourceserver.cors;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_cors_origin",
	uniqueConstraints = {@UniqueConstraint(name = "u_schema_host_port", columnNames = {"schema", "host", "port"})})
@Getter
@EqualsAndHashCode(of = {"origin"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CorsOrigin implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "schema", column = @Column(name = "schema")),
		@AttributeOverride(name = "host", column = @Column(name = "host")),
		@AttributeOverride(name = "port", column = @Column(name = "port"))
	})
	private Origin origin;

	public static CorsOrigin create(Origin origin) {
		CorsOrigin corsOrigin = new CorsOrigin();
		corsOrigin.origin = origin;
		return corsOrigin;
	}
}
