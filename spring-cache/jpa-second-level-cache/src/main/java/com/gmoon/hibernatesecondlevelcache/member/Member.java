package com.gmoon.hibernatesecondlevelcache.member;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Member implements Serializable {

	private Long id;
	private String name;

}
