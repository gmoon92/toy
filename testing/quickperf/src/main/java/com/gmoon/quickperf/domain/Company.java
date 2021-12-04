package com.gmoon.quickperf.domain;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@ToString(exclude = "teams")
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company {

  @Id
  @GeneratedValue
  private Long id;

  @OneToMany(mappedBy = "company")
  private List<Team> teams = new ArrayList<>();

  private String name;

  public static Company create(String name) {
    Company company = new Company();
    company.name = name;
    return company;
  }

  void addTeam(Team team) {
    teams.add(team);
  }
}
