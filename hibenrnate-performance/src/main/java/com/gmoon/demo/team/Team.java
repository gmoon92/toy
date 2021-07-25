package com.gmoon.demo.team;

import com.gmoon.demo.apply_form.ApplyForm;
import com.gmoon.demo.member.domain.Member;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team {

  @Id
  @GeneratedValue
  private Long id;

  @Column
  private String name;

  @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Member> members = new ArrayList<>();

  @OneToMany(mappedBy = "team", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<ApplyForm> applyForms = new ArrayList<>();

  public static Team newInstance(String name) {
    Team newTeam = new Team();
    newTeam.name = name;
    return newTeam;
  }
}
