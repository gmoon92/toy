package com.gmoon.demo.member.domain;

import com.gmoon.demo.member.model.MemberOptionUpdate;
import com.gmoon.demo.team.Team;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToOne;

@Entity
@Getter
@NamedEntityGraph(name = "Member.withMemberOption"
        , attributeNodes = { @NamedAttributeNode(value = "memberOption") })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Member {

  @Id
  @GeneratedValue
  private Long id;

  @Column
  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  private Team team;

  @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, optional = false)
//    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, optional = false, fetch = FetchType.LAZY)
  private MemberOption memberOption;

  private Member(String name, Team team) {
    this.name = name;
    this.team = team;
    this.memberOption = MemberOption.defaultOption(this);
  }

  public static Member newInstance(String name, Team team) {
    return new Member(name, team);
  }

  public void setName(String name) {
    this.name = name;
  }

  public void changeMemberOption(MemberOptionUpdate memberOptionUpdate) {
    memberOption.changeOptions(memberOptionUpdate);
  }

  public void enabled() {
    memberOption.enabled();
  }

  public void disabled() {
    memberOption.disabled();
  }

}
