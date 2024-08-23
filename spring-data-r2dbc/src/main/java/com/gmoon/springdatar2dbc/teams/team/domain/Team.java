package com.gmoon.springdatar2dbc.teams.team.domain;

import java.io.Serializable;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@org.springframework.data.relational.core.mapping.Table(name = "tb_team")
@Entity
@Table(name = "tb_team")
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class Team implements Serializable {

	@Id
	@UuidGenerator
	@Column(name = "id", length = 50)
	private String id;

	@Column(name = "name", length = 10)
	private String name;

	@ManyToOne
	@JoinColumn(name = "parent_id", referencedColumnName = "id")
	private Team parent;
}
