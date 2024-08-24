package com.gmoon.hibernatesecondlevelcache.global.code;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@Immutable
@Entity(name = "tb_common_code")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CommonCode {

	@Id
	@Column(nullable = false)
	@EqualsAndHashCode.Include
	private String code;

	@Column(nullable = false)
	private String value;

	private String description;

	@Builder
	private CommonCode(String code, String value, String description) {
		this.code = code;
		this.value = value;
		this.description = description;
	}
}
