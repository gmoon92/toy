package com.gmoon.demo.member.model;

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
public class EmbeddedMemberOption {

	private boolean enabled;
	private LocalDateTime enabledDt;

	public static EmbeddedMemberOption enabled() {
		EmbeddedMemberOption accountOption = new EmbeddedMemberOption();
		accountOption.enabled = true;
		accountOption.enabledDt = LocalDateTime.now();
		return accountOption;
	}

	public static EmbeddedMemberOption disabled() {
		EmbeddedMemberOption accountOption = new EmbeddedMemberOption();
		accountOption.enabled = false;
		accountOption.enabledDt = null;
		return accountOption;
	}
}
