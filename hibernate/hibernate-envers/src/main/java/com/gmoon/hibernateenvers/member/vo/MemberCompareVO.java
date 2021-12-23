package com.gmoon.hibernateenvers.member.vo;

import com.gmoon.hibernateenvers.global.vo.BaseRevisionCompareVO;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@EqualsAndHashCode(exclude = "id")
public class MemberCompareVO implements BaseRevisionCompareVO {

	private String name;

	private String password;

}
