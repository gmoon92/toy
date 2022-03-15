package com.gmoon.resourceserver.cors;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.http.HttpMethod;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "tb_access_control_allow_method")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CorsHttpMethod implements Serializable {
	@Id
	@Enumerated(EnumType.STRING)
	@Column(name = "id", length = 50)
	private HttpMethod id;

	private boolean enabled;

	private CorsHttpMethod(HttpMethod id, boolean enabled) {
		this.id = id;
		this.enabled = enabled;
	}

	public static CorsHttpMethod create(HttpMethod httpMethod) {
		return new CorsHttpMethod(httpMethod, false);
	}

	public CorsHttpMethod enabled() {
		enabled = true;
		return this;
	}

	public CorsHttpMethod disabled() {
		enabled = false;
		return this;
	}
}
