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

import lombok.EqualsAndHashCode;

@Entity
@Table(name = "tb_cors_origin",
	uniqueConstraints = {@UniqueConstraint(name = "u_schema_host_port", columnNames = {"schema", "host", "port"})})
@EqualsAndHashCode(of = {"origin"})
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
}
