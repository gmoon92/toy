package com.gmoon.demo.member.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberOptionUpdateRequestVO {

	private Long memberId;
	private boolean retired;

}
