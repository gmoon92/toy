package com.gmoon.demo.member.domain;

import com.gmoon.demo.apply_form.ApplyForm;
import com.gmoon.demo.member.model.MemberOptionUpdate;
import com.gmoon.demo.team.TeamMember;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

  @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, optional = false)
//    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, optional = false, fetch = FetchType.LAZY)
  private MemberOption memberOption;

  @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
  private List<ApplyForm> applyForm = new ArrayList<>();

  @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
  private Set<TeamMember> teamMembers = new HashSet<>();

  private Member(String name) {
    this.name = name;
    defaultOption();
  }

  public static Member newInstance(String name) {
    return new Member(name);
  }

  private void defaultOption() {
    memberOption = MemberOption.defaultOption(this);
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