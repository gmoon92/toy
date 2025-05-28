package com.gmoon.batchinsert.coupons.domain.vo;

import java.time.Instant;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@NoArgsConstructor
@Getter
@Setter
public class RegisteredBy {

	private String registeredBy;
	private Instant registeredAt;
}
