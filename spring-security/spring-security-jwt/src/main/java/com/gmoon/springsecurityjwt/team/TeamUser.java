package com.gmoon.springsecurityjwt.team;

import java.io.Serializable;

import com.gmoon.springsecurityjwt.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "tb_team_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TeamUser implements Serializable {
	private static final long serialVersionUID = -273473458581806379L;

	@EmbeddedId
	@EqualsAndHashCode.Include
	private Id id;

	@ToString.Exclude
	@MapsId("teamId")
	@ManyToOne(optional = false)
	@JoinColumn(name = "team_id", insertable = false, updatable = false)
	private Team team;

	@ToString.Exclude
	@MapsId("userId")
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
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@EqualsAndHashCode
	static class Id implements Serializable {
		private static final long serialVersionUID = -6697347285557271747L;

		@Column(nullable = false)
		private Long teamId;

		@Column(nullable = false)
		private Long userId;

		Id(Long teamId, Long userId) {
			this.teamId = teamId;
			this.userId = userId;
		}
	}
}
