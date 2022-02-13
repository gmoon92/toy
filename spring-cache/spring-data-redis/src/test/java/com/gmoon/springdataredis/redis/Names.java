package com.gmoon.springdataredis.redis;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
class Names implements Serializable {

	private List<String> value;

	static Names from(String... name) {
		Names names = new Names();
		names.value = Arrays.asList(name);
		return names;
	}
}
