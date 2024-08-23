package com.gmoon.hibernateenvers.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("CUSTOM")
public class MemberCustom extends Member {

	@Column
	private String accessAccountFailCount;

}
