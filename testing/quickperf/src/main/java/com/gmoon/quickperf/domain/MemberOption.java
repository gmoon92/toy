package com.gmoon.quickperf.domain;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;

@Getter
@Entity
@ToString(exclude = "member")
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberOption {

  @Id
  private Long id;

  @MapsId
  @OneToOne
  private Member member;

  private boolean enabled;

  static MemberOption defaultOption(Member member) {
    MemberOption option = new MemberOption();
    option.member = member;
    option.enabled = true;
    return option;
  }
}
