package com.gmoon.hibernateperformance.member.domain;

import java.time.Instant;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Enabled {

	@EqualsAndHashCode.Include
	private boolean enabled;
	private Instant enabledAt;

	public static Enabled enabled() {
		Enabled accountOption = new Enabled();
		accountOption.enabled = true;
		accountOption.enabledAt = Instant.now();
		return accountOption;
	}

	public static Enabled disabled() {
		Enabled accountOption = new Enabled();
		accountOption.enabled = false;
		accountOption.enabledAt = null;
		return accountOption;
	}
}
