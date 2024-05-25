package com.gmoon.springsecurityjwt.team;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.querydsl.core.annotations.QueryProjection;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Table(name = "tb_team", uniqueConstraints = {@UniqueConstraint(name = "u_name", columnNames = {"name"})})
@Entity
@Getter
@JsonIgnoreProperties({"users"})
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(of = "name")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team implements Serializable {
	private static final long serialVersionUID = 722253006387150023L;

	@Id
	@GeneratedValue
	private Long id;

	@ToString.Include
	@Column(nullable = false)
	private String name;

	@OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<TeamUser> users = new ArrayList<>();

	public static Team create(String name) {
		Team team = new Team();
		team.name = name;
		return team;
	}

	@QueryProjection
	public Team(Long id, String name) {
		this.id = id;
		this.name = name;
	}

	public Team clearUsers() {
		users.clear();
		return this;
	}
}
