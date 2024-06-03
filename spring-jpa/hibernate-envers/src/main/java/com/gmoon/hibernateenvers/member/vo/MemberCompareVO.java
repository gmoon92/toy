package com.gmoon.hibernateenvers.member.vo;

import com.gmoon.hibernateenvers.global.vo.BaseRevisionCompareVO;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@ToString
public class MemberCompareVO implements BaseRevisionCompareVO {

	private String name;

	private String password;

}
