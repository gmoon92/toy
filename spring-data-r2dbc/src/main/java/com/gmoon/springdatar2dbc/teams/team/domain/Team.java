package com.gmoon.springdatar2dbc.teams.team.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@org.springframework.data.relational.core.mapping.Table(name = "tb_team")
@Table(name = "tb_team")
@Entity
@NoArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class Team implements Serializable {

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid2")
	@Column(name = "id", length = 50)
	private String id;

	@Column(name = "name", length = 10)
	private String name;

	@ManyToOne
	@JoinColumn(name = "parent_id", referencedColumnName = "id")
	private Team parent;
}
