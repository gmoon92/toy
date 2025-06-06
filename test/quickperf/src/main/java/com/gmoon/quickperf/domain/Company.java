package com.gmoon.quickperf.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
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
