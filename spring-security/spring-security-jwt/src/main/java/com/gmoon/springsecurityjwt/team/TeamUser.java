package com.gmoon.springsecurityjwt.team;

import java.io.Serializable;

import com.gmoon.springsecurityjwt.user.User;

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

@Getter
@Entity
@Table(name = "tb_team_user")
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamUser implements Serializable {
	private static final long serialVersionUID = -273473458581806379L;

	@EmbeddedId
	private Id id;

	@ToString.Exclude
	@ManyToOne(optional = false)
	@JoinColumn(name = "team_id", insertable = false, updatable = false)
	private Team team;

	@ToString.Exclude
	@ManyToOne(optional = false)
	@JoinColumn(name = "user_id", insertable = false, updatable = false)
	private User user;

	private TeamUser(Id id) {
		this.id = id;
	}

	public static TeamUser create(Team team, User user) {
		Id id = new Id(team.getId(), user.getId());
		return new TeamUser(id);
	}

	@Embeddable
	@EqualsAndHashCode
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	static class Id implements Serializable {
		private static final long serialVersionUID = -6697347285557271747L;

		@Column(name = "team_id", nullable = false)
		private Long teamId;

		@Column(name = "user_id", nullable = false)
		private Long userId;

		Id(Long teamId, Long userId) {
			this.teamId = teamId;
			this.userId = userId;
		}
	}
}
