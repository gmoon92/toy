package com.gmoon.hibernatesecondlevelcache.member;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Member implements Serializable {

	private Long id;
	private String name;

}
