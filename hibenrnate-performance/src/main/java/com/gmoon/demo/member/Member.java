package com.gmoon.demo.member;

import com.gmoon.demo.member.model.MemberOptionUpdate;
import com.gmoon.demo.team.Team;
import lombok.AccessLevel;
import lombok.Builder;
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

  @Builder(access = AccessLevel.PRIVATE, builderMethodName = "defaultMember")
  protected Member(String name, Team team) {
    this.name = name;
    this.team = team;
    setMemberOption(MemberOption.defaultOption());
  }

  public static Member newInstance(String name) {
    return Member.defaultMember()
            .name(name)
            .build();
  }

  public static Member newInstance(String name, Team team) {
    return Member.defaultMember()
            .name(name)
            .team(team)
            .build();
  }

  public void changeMemberInfo(String name) {
    this.name = name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setMemberOption(MemberOption memberOption) {
    this.memberOption = memberOption;
    memberOption.setMember(this);
  }

  public void changeMemberOption(MemberOptionUpdate memberOptionUpdate) {
    memberOption.changeOptions(memberOptionUpdate);
  }

}
