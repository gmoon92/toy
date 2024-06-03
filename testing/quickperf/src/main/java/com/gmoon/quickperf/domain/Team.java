package com.gmoon.quickperf.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
@ToString(exclude = "members")
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
