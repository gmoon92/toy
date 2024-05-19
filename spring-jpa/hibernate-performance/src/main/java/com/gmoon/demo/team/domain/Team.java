package com.gmoon.demo.team.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.gmoon.demo.applyform.domain.ApplyForm;
import com.gmoon.demo.member.domain.Member;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
			 .collect(Collectors.toList()));
	}
}
