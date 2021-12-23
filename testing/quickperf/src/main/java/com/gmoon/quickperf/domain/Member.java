package com.gmoon.quickperf.domain;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Entity
@ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

	@Id
	@GeneratedValue
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
