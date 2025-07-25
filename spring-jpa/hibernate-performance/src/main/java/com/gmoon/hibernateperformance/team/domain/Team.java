package com.gmoon.hibernateperformance.team.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.gmoon.hibernateperformance.applyform.domain.ApplyForm;
import com.gmoon.hibernateperformance.member.domain.Member;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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
public class Team {

	@Id
	@GeneratedValue
	private Long id;

	@Column
	private String name;

	@OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<TeamMember> teamMembers = new HashSet<>();

	@OneToMany(mappedBy = "team", cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<ApplyForm> applyForms = new ArrayList<>();

	public static Team newInstance(String name) {
		Team newTeam = new Team();
		newTeam.name = name;
		return newTeam;
	}

	public void addMembers(Collection<Member> managedMembers) {
		this.teamMembers.clear();
		this.teamMembers.addAll(managedMembers.stream()
			 .map(member -> new TeamMember(member, this))
			 .toList());
	}
}
