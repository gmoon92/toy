package com.gmoon.hibernateperformance.applyform.domain;

import com.gmoon.hibernateperformance.global.base.EntityId;
import com.gmoon.hibernateperformance.member.domain.Member;
import com.gmoon.hibernateperformance.team.domain.Team;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class ApplyForm {

	@EmbeddedId
	private Id id = new Id();

	@ToString.Exclude
	@ManyToOne(optional = false)
	@JoinColumn(name = "member_id", insertable = false, updatable = false)
	private Member member;

	@ToString.Exclude
	@ManyToOne(optional = false)
	@JoinColumn(name = "team_id", insertable = false, updatable = false)
	private Team team;

	private String title;

	private String content;

	ApplyForm(Member member, Team team) {
		setMember(member);
		setTeam(team);
		this.id = new Id(member, team);
	}

	// 객체 참조 추가
	void setMember(Member member) {
		this.member = member;
		member.getApplyForm().add(this);
	}

	void setTeam(Team team) {
		this.team = team;
		team.getApplyForms().add(this);
	}

	public static ApplyForm newInstance(Member member, Team team) {
		return new ApplyForm(member, team);
	}

	public void fillOut(String title, String content) {
		this.title = title;
		this.content = content;
	}

	@Embeddable
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@EqualsAndHashCode
	static class Id extends EntityId {

		private static final long serialVersionUID = -4188030215536759764L;

		@Column(name = "member_id")
		private Long member;

		@Column(name = "team_id")
		private Long team;

		Id(Member member, Team team) {
			this.member = member.getId();
			this.team = team.getId();
		}
	}

}
