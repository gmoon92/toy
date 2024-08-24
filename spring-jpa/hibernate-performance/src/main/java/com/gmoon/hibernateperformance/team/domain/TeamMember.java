package com.gmoon.hibernateperformance.team.domain;

import java.io.Serializable;

import com.gmoon.hibernateperformance.member.domain.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TeamMember {

	@EmbeddedId
	@EqualsAndHashCode.Include
	private Id id = new Id();

	@MapsId("memberId")
	@ManyToOne(optional = false)
	@JoinColumn(name = "member_id", referencedColumnName = "id", insertable = false, updatable = false)
	private Member member;

	@MapsId("teamId")
	@ManyToOne(optional = false)
	@JoinColumn(name = "team_id", referencedColumnName = "id", insertable = false, updatable = false)
	private Team team;

	public TeamMember(Member member, Team team) {
		this.member = member;
		this.team = team;
		this.id = new Id(member, team);
	}

	@Embeddable
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@EqualsAndHashCode
	public static class Id implements Serializable {

		@Column(nullable = false)
		private Long memberId;

		@Column(nullable = false)
		private Long teamId;

		Id(Member member, Team team) {
			this.memberId = member.getId();
			this.teamId = team.getId();
		}
	}

}
