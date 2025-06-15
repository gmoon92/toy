package com.gmoon.test.fixturemonkey.common;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString
public class Address {
	private String postalCode;
	private String address1;
	private String address2;
	private String city;
	private String state;
	private String country;
}
