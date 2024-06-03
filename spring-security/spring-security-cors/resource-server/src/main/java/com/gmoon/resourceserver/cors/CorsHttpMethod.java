package com.gmoon.resourceserver.cors;

import java.io.Serializable;

import org.springframework.http.HttpMethod;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_access_control_allow_method")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CorsHttpMethod implements Serializable {
	@Id
	@Column(name = "id", length = 50)
	private String httpMethod;

	private boolean enabled;

	private CorsHttpMethod(HttpMethod httpMethod, boolean enabled) {
		this.httpMethod = httpMethod.name();
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
