package com.gmoon.resourceserver.cors;

import java.io.Serializable;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_cors_origin",
	 uniqueConstraints = {@UniqueConstraint(name = "u_schema_host_port", columnNames = {"schema", "host", "port"})})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
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
	@EqualsAndHashCode.Include
	private Origin origin;

	public static CorsOrigin create(Origin origin) {
		CorsOrigin corsOrigin = new CorsOrigin();
		corsOrigin.origin = origin;
		return corsOrigin;
	}
}
