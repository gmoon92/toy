package com.gmoon.test.fixturemonkey.orders;

import com.gmoon.test.fixturemonkey.common.Address;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString
public class Recipient {
	private String id;
	private String name;
	private String phone;
	private Address address;
	private String deliveryMemo;
}
