package com.gmoon.demo.member.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "enabled")
@ToString
public class Enabled {

	private boolean enabled;
	private LocalDateTime enabledDt;

	public static Enabled enabled() {
		Enabled accountOption = new Enabled();
		accountOption.enabled = true;
		accountOption.enabledDt = LocalDateTime.now();
		return accountOption;
	}

	public static Enabled disabled() {
		Enabled accountOption = new Enabled();
		accountOption.enabled = false;
		accountOption.enabledDt = null;
		return accountOption;
	}
}
