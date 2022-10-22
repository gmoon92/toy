package com.gmoon.demo.applyform.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.gmoon.demo.global.base.EntityId;
import com.gmoon.demo.member.domain.Member;
import com.gmoon.demo.team.domain.Team;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Table
@Entity
@Getter
@EqualsAndHashCode(of = "id")
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
	@EqualsAndHashCode
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
