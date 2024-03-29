package com.gmoon.demo.member.domain;

import java.time.LocalDateTime;

import javax.persistence.Embeddable;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Embeddable
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "enabled")
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
