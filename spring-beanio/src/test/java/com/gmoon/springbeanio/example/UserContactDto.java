package com.gmoon.springbeanio.example;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserContactDto {
	private String firstName;
	private String lastName;
	private String street;
	private String city;
	private String state;
	private String zip;
}
