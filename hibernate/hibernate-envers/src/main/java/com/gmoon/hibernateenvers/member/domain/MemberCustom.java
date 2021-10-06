package com.gmoon.hibernateenvers.member.domain;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("CUSTOM")
public class MemberCustom extends Member {

  @Column(name = "access_account_fail_count")
  private String accessAccountFailCount;

}