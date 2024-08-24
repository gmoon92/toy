package com.gmoon.hibernateenvers.member.vo;

import com.gmoon.hibernateenvers.global.vo.BaseRevisionCompareVO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberCompareVO implements BaseRevisionCompareVO {

	private String name;

	private String password;

}
