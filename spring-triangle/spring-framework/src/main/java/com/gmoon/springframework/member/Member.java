package com.gmoon.springframework.member;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Member {

	private String name;
	private LocalDateTime createdDt;
	private MemberStatus status;
}
