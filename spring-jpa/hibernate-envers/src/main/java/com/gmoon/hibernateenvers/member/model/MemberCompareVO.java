package com.gmoon.hibernateenvers.member.model;

import com.gmoon.hibernateenvers.global.model.BaseRevisionCompareVO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberCompareVO implements BaseRevisionCompareVO {

	private String name;

	private String password;

}
