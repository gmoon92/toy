package com.gmoon.querydslprojections.movies.users.user.domain;

import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "tb_actor")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
public class Actor extends User {

	public Actor(String id, String name) {
		this.id = id;
		this.name = name;
	}
}
