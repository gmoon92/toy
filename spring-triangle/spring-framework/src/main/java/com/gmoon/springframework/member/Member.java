package com.gmoon.springframework.member;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Member {

	private String name;
	private Instant createdAt;
	private MemberStatus status;
}
