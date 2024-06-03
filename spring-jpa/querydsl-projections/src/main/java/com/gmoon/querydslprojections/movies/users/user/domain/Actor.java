package com.gmoon.querydslprojections.movies.users.user.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_actor")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
public class Actor extends User {

	public Actor(String id, String name) {
		this.id = id;
		this.name = name;
	}
}
