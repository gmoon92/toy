package com.gmoon.quickperf.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Member {

	@Id
	@GeneratedValue
	@EqualsAndHashCode.Include
	private Long id;

	private String name;

	@ManyToOne
	private Team team;

	@OneToOne(mappedBy = "member", cascade = CascadeType.ALL,
		 optional = false, fetch = FetchType.LAZY)
	private MemberOption memberOption;

	public static Member create(Team team, String name) {
		Member member = new Member();
		member.name = name;
		member.team = team;
		team.addMember(member);
		member.defaultOption();
		return member;
	}

	void defaultOption() {
		memberOption = MemberOption.defaultOption(this);
	}

	public void removeTeam() {
		team = null;
	}
}
