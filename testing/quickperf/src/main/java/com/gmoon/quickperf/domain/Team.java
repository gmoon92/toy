package com.gmoon.quickperf.domain;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@ToString(exclude = "members")
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team {

  @Id
  @GeneratedValue
  private Long id;

  private String name;

  @ManyToOne
  private Company company;

  @OneToMany(mappedBy = "team")
  private List<Member> members = new ArrayList<>();

  public static Team create(Company company, String name) {
    Team team = new Team();
    team.name = name;
    team.company = company;
    company.addTeam(team);
    return team;
  }

  public void addMember(Member member) {
    members.add(member);
  }
}
