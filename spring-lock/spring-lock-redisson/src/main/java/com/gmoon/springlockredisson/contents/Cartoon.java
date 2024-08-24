package com.gmoon.springlockredisson.contents;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Cartoon implements Serializable {

	@EqualsAndHashCode.Include
	private Long id;
	private Long count;

	public Cartoon(Long id) {
		this.id = id;
		this.count = 0l;
	}

	public void hit() {
		count++;
	}
}
