package com.gmoon.demo.team.domain;

import java.io.Serializable;

import com.gmoon.demo.member.domain.Member;

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
import lombok.ToString;

@Entity
@ToString
@Getter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamMember {

	@EmbeddedId
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
	@ToString
	@EqualsAndHashCode
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	public static class Id implements Serializable {

		@Column(name = "member_id", nullable = false)
		private Long memberId;

		@Column(name = "team_id", nullable = false)
		private Long teamId;

		Id(Member member, Team team) {
			this.memberId = member.getId();
			this.teamId = team.getId();
		}
	}

}
