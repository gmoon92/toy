package com.gmoon.test.fixturemonkey.orders;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString
public class OrderBuyer {
	private String id;
	private String name;
	private String email;
	private String phone;
}
